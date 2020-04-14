package com.takiku.im_lib.call;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.interceptor.BridgeInterceptor;
import com.takiku.im_lib.interceptor.CacheInterceptor;
import com.takiku.im_lib.interceptor.CallServerInterceptor;
import com.takiku.im_lib.interceptor.ConnectInterceptor;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.interceptor.RealInterceptorChain;
import com.takiku.im_lib.interceptor.RetryAndFollowUpInterceptor;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RealCall implements Call {
    final IMClient client;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    private boolean executed;
    public RealCall(IMClient client, Request originalRequest){
     this.client=client;
     this.originalRequest=originalRequest;
     this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client);
     eventListener=client.eventListenerFactory().create(this);
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }
    static RealCall newRealCall(IMClient client, Request originalRequest) {
        // Safely publish the Call instance to the EventListener.
        RealCall call = new RealCall(client, originalRequest);
        call.eventListener = client.eventListenerFactory().create(call);
        return call;
    }
    @Override
    public Call clone() {
        return RealCall.newRealCall(client, originalRequest);
    }
   public final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback) {
            super("IMClient %s",redactedUrl());
            this.responseCallback=responseCallback;
        }
        String host(){return originalRequest.address.getUrl();}

        Request request(){return originalRequest;}

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
         try{
             Response response = getResponseWithInterceptorChain();
             if (retryAndFollowUpInterceptor.isCanceled()){
                 signalledCallback = true;
                 responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
             }else {
                 signalledCallback = true;
                 responseCallback.onResponse(RealCall.this, response);
             }
         }catch ( InterruptedException e){

         }catch (IOException e){
             if (signalledCallback) {
                 // Do not signal the callback twice!
                 // Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
             } else {
                 responseCallback.onFailure(RealCall.this, e);
             }
         }
         finally {
          client.dispatcher().finished(this);
         }
        }
    }
    String redactedUrl() {
        return originalRequest.address.getUrl();
    }

    Response getResponseWithInterceptorChain() throws IOException, InterruptedException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        if (client.interceptors()!=null&&client.interceptors().size()>0){
            interceptors.addAll(client.interceptors());
        }
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client));
       // interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectInterceptor(client));
        interceptors.add(new CallServerInterceptor());


        Interceptor.Chain chain = new RealInterceptorChain(
                interceptors, null, null, null, 0, originalRequest,this, eventListener,client.connectTimeout(),
                client.resendCount());
        return chain.proceed(originalRequest);
    }
}
