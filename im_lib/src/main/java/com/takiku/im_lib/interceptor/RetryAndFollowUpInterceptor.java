package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.exception.RouteException;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.entity.UnrepeatableRequestBody;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.ConnectionShutdownException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public class RetryAndFollowUpInterceptor implements Interceptor {

    private final IMClient client;
    private StreamAllocation streamAllocation;
    private Object callStackTrace;
    private static final int MAX_FOLLOW_UPS = 20;
    private volatile boolean canceled;
    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public RetryAndFollowUpInterceptor(IMClient imClient){
        this.client=imClient;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        streamAllocation = new StreamAllocation(client.connectionPool(), request.address,client.customChannelHandlerLinkedHashMap(), callStackTrace);
        int followUpCount = 0;
        int resendCount=0;
        while (true){
            if (canceled) {
                streamAllocation.release();
                throw new IOException("Canceled");
            }
            Response response = null;
            boolean releaseConnection = true;
            try {
                response = ((RealInterceptorChain) chain).proceed(request, streamAllocation, null, null);
                releaseConnection = false;
            } catch (RouteException e) {
                // The attempt to connect via a route failed. The request will not have been sent.
                if (!recover(e.getLastConnectException(), false, request)) {
                    throw e.getLastConnectException();
                }
                releaseConnection = false;
                if (++resendCount<client.resendCount()) //未达到重复次数上线继续重试
                continue;
            } catch (IOException e) {
                // An attempt to communicate with a server failed. The request may have been sent.
                //先判断当前请求是否已经发送了
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recover(e, requestSendStarted, request)) throw e;
                releaseConnection = false;
                if (++resendCount<client.resendCount())
                    continue;
                continue;
            } catch (InterruptedException e) {
                System.out.println(" InterruptedException ");
                e.printStackTrace();
            } catch (AuthException e)  {
                e.printStackTrace();
            }finally {
                // We're throwing an unchecked exception. Release any resources.
                if (releaseConnection) {   //未捕获到，释放资源
                    streamAllocation.streamFailed(null);
                    streamAllocation.release();
                }
            }

            Request followUp = followUpRequest(response);
            if (followUp==null){
                return response;
            }
            if (++followUpCount >MAX_FOLLOW_UPS) {
                streamAllocation.release();
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

        }
    }

    private Request followUpRequest(Response response) {
          if (response!=null){
              return null;
          }
      return   response.request;
    }

    /**
     * 是否能恢复
     * @param e
     * @param requestSendStarted
     * @param userRequest
     * @return
     */
    private boolean recover(IOException e, boolean requestSendStarted, Request userRequest) {
        streamAllocation.streamFailed(e);

        // The application layer has forbidden retries.
        if (client.resendCount()<=0) return false;

        // We can't send the request body again.
        if (requestSendStarted && userRequest.body instanceof UnrepeatableRequestBody) return false;

        // This exception is fatal.
        if (!isRecoverable(e, requestSendStarted)) return false;

        // No more routes to attempt.
        if (!streamAllocation.hasMoreRoutes()) return false;

        // For failure recovery, use the same route selector with a new connection.
        return true;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        // If there was a protocol problem, don't recover.
        if (e instanceof ProtocolException) {
            return false;
        }

        // If there was an interruption don't recover, but if there was a timeout connecting to a route
        // we should try the next route (if there is one).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException && !requestSendStarted;
        }

        // Look for known client-side or negotiation errors that are unlikely to be fixed by trying
        // again with a different route.
        if (e instanceof SSLHandshakeException) {
            // If the problem was a CertificateException from the X509TrustManager,
            // do not retry.
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

        // An example of one we might want to retry with a different route is a problem connecting to a
        // proxy and would manifest as a standard IOException. Unless it is one we know we should not
        // retry, we return true and try a new route.
        return true;
    }
    public boolean isCanceled() {
        return canceled;
    }
}
