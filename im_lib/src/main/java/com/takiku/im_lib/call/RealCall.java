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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RealCall implements Call {
    final IMClient client;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private boolean executed;
    RealCall(IMClient client,Request originalRequest){
     this.client=client;
     this.originalRequest=originalRequest;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client);
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
    }

    @Override
    public Call clone() {
        return null;
    }
   public final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback) {
            super("IMClient %s",redactedUrl());
            this.responseCallback=responseCallback;
        }
        String host(){return originalRequest.url;}

        Request request(){return originalRequest;}

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
         try{
             Response response = getResponseWithInterceptorChain();
         }catch (IOException e){
             if (signalledCallback) {
                 // Do not signal the callback twice!
                // Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
             } else {
                 responseCallback.onFailure(RealCall.this, e);
             }
         }finally {
          client.dispatcher().finished(this);
         }
        }
    }
    String redactedUrl() {
        return originalRequest.url;
    }

    Response getResponseWithInterceptorChain() throws IOException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor());
        interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectInterceptor(client));
        interceptors.add(new CallServerInterceptor());


        Interceptor.Chain chain = new RealInterceptorChain(
                interceptors, null, null, null, 0, originalRequest);
        return chain.proceed(originalRequest);
    }
}
