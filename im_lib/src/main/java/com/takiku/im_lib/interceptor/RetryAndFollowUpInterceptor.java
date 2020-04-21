package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.exception.RouteException;
import com.takiku.im_lib.exception.SendTimeoutException;
import com.takiku.im_lib.internal.connection.StreamAllocation;

import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

public class RetryAndFollowUpInterceptor implements Interceptor {

    private final IMClient client;
    private StreamAllocation streamAllocation;
    private Object callStackTrace;
    private static final int MAX_CONNECT_RETRY=3;
    private volatile boolean canceled;
    int connect_retry=0;
    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public RetryAndFollowUpInterceptor(IMClient imClient){
        this.client=imClient;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = chain.request();
        streamAllocation = new StreamAllocation(client.connectionPool(),client.addressList(),client.customChannelHandlerLinkedHashMap(), callStackTrace);
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

            } catch (IOException e) {
                if (request instanceof ConnectRequest){ //连接请求重试
                    if (!connectRecover(e,request, ++connect_retry)) {
                        realChain.eventListener().connectFailed(streamAllocation.currentInetSocketAddress(),e);
                        throw e;
                    }
                       System.out.println("连接重试 " + streamAllocation.currentInetSocketAddress().toString());
                       releaseConnection=false;
                       continue;
                }
                if (!sendRecover(e,  request,++resendCount)){
                    realChain.eventListener().sendMsgFailed(realChain.call());
                    throw e;
                }
                      //发送请求重试
                       System.out.println("发送重试");
                       releaseConnection=false;
                       continue;

            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (AuthException e)  {
                e.printStackTrace();
            }finally {
                // We're throwing an unchecked exception. Release any resources.
                if (releaseConnection) {   //是否需要释放连接
                    streamAllocation.release();
                }
            }
            if (isOk(response)){ //拿到正确response直接返回
                return response;
            }
        }
    }

    private boolean isOk(Response response) {
        if (response!=null&&response.code==Response.SUCCESS){
            return true;
        }else {
            return false;
        }
    }

    //切换下一个地址
    private Request followUpRequest(Request request) throws IOException {
        if (!streamAllocation.hasMoreRoutes()) return null;

         streamAllocation.nextRoute();
      return   request;
    }

    /**
     * 发送请求是否能恢复
     * @param e
     * @param userRequest
     * @return
     */
    private boolean sendRecover(IOException e,  Request userRequest,int sendCount) {


        // The application layer has forbidden retries.
        if (sendCount>client.resendCount()) return false;

        // We can't send the request body again.
        if (!userRequest.sendRetry) return false;

        // This exception is fatal.
        if (!isRecoverable(e)) return false;
        return true;
    }

    /**
     * 连接请求是否能恢复
     * @param e
     * @param userRequest
     * @return
     */
    private boolean connectRecover(IOException e,  Request userRequest,int  connectCount) throws IOException {

        // The application layer has forbidden retries.
        if (!client.connectionRetryEnabled()) return false;


        if (connectCount>MAX_CONNECT_RETRY){
            connect_retry=1;
             Request request=followUpRequest(userRequest);
            if (request==null) return false;

        }

        // This exception is fatal.
        if (!isRecoverable(e)) return false;
            return true;
    }

    private boolean isRecoverable(IOException e) {
        // If there was a protocol problem, don't recover.
        if (e instanceof ProtocolException) {
            return false;
        }

        // If there was an interruption don't recover, but if there was a timeout connecting to a route
        // we should try the next route (if there is one).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException;
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
