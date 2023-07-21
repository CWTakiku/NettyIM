package com.takiku.im_lib.call;

import android.util.Log;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.exception.SendTimeoutException;
import com.takiku.im_lib.interceptor.BridgeInterceptor;
import com.takiku.im_lib.interceptor.CallServerInterceptor;
import com.takiku.im_lib.interceptor.ConnectInterceptor;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.interceptor.RealInterceptorChain;
import com.takiku.im_lib.interceptor.RetryAndFollowUpInterceptor;
import com.takiku.im_lib.internal.Internal;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RealCall implements Call {
    final IMClient client;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    private boolean executed;
    private List<Consumer> consumers;
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
    public Call enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
        return this;
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



    @Override
    public Disposable subscribe(Consumer... consumers) {
        this.consumers= new ArrayList<>(Arrays.asList(consumers));
        return new MessageSubscriber(this);
    }

    @Override
    public Disposable subscribe(List<Consumer> consumerList) {
        this.consumers= consumerList;
        return new MessageSubscriber(this);
    }


    protected void dispose(){
        RealConnection realConnection= client.connectionPool().realConnection();
        if (realConnection!=null){
            realConnection.unRegisterConsumer(request());
        }
    }
    protected boolean isDisposed(){
        RealConnection realConnection= client.connectionPool().realConnection();
        if (realConnection!=null){
          return   realConnection.isRegister(request());
        }else {
            return true;
        }
    }

    public final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback) {
            super("IMClient requestType %s",redactedRequest());
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
             Response response = getResponseWithInterceptorChain(consumers);
             if (retryAndFollowUpInterceptor.isCanceled()){
                 signalledCallback = true;

                 responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
             }else {
                 signalledCallback = true;

                 responseCallback.onResponse(RealCall.this, response);
             }
         }catch ( InterruptedException e){
             e.printStackTrace();
         }catch (IOException e){
             e.printStackTrace();
             if (signalledCallback) {
                 // Do not signal the callback twice!
                 // Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
             } else {
                 responseCallback.onFailure(RealCall.this, e);
             }
         }         catch (AuthException e) {
          //   e.printStackTrace();
         } finally {
          client.dispatcher().finished(this);
         }
        }
    }
    String redactedRequest() {
        if (originalRequest.requestTag!=null){
            return originalRequest.requestTag;
        }else {
            return "requestTag null";
        }

    }

    Response getResponseWithInterceptorChain(List<Consumer> consumers) throws IOException, InterruptedException, AuthException, SendTimeoutException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        if (client.interceptors()!=null&&client.interceptors().size()>0){
            interceptors.addAll(client.interceptors());
        }
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client));
       // interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectInterceptor(client));
        interceptors.add(new CallServerInterceptor(consumers));


        Interceptor.Chain chain = new RealInterceptorChain(
                interceptors, null, null, null, 0, originalRequest,this, eventListener,client.connectTimeout(),
                client.sendTimeout());
        return chain.proceed(originalRequest);
    }
}
