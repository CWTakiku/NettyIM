package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.internal.connection.TcpStream;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;

public class ConnectInterceptor implements Interceptor {

    public final IMClient client;
    public ConnectInterceptor(IMClient imClient){
        this.client=imClient;
    }
    @Override
    public Response intercept(Chain chain) throws IOException, InterruptedException, AuthException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        EventListener eventListener=realChain.eventListener();
        TcpStream tcpStream= streamAllocation.newStream(client, chain,eventListener);
        return realChain.proceed(request,streamAllocation,tcpStream,streamAllocation.connection());
    }
}
