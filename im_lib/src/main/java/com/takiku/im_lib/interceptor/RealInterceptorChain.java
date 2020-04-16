package com.takiku.im_lib.interceptor;

import androidx.annotation.Nullable;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.connection.TcpStream;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RealInterceptorChain implements Interceptor.Chain {
    private final Request request;
    private final List<Interceptor> interceptors;
    private final int index;
    private final TcpStream tcpStream;
    private final StreamAllocation streamAllocation;
    private final RealConnection connection;
    private final EventListener eventListener;
    private final int connectTimeout;
    private final int sendTimeout;
    private final Call call;
    private int calls;
    public RealInterceptorChain(List<Interceptor> interceptors,
                                StreamAllocation streamAllocation,
                                TcpStream tcpStream, RealConnection realConnection,
                                int index, Request request, Call call, EventListener eventListener,
                                int connectTimeout,int sendTimeout){
        this.request=request;
        this.index=index;
        this.interceptors=interceptors;
        this.tcpStream=tcpStream;
        this.streamAllocation=streamAllocation;
        this.connection=realConnection;
        this.connectTimeout=connectTimeout;
        this.sendTimeout=sendTimeout;
        this.eventListener = eventListener;
        this.call = call;
    }

    @Nullable
    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    @Override
    public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public int sendTimeoutMillis() {
        return sendTimeout;
    }

    @Override
    public Interceptor.Chain withSendTimeoutMillis(int timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException, InterruptedException, AuthException {
        return proceed(request, streamAllocation, tcpStream, connection);
    }

    @Override
    public Call call() {
        return call;
    }

    public Response proceed(Request request, StreamAllocation streamAllocation, TcpStream tcpStream,
                            RealConnection connection) throws IOException, InterruptedException, AuthException {
        if (index >= interceptors.size()) throw new AssertionError();
        calls++;
        // Call the next interceptor in the chain.
        RealInterceptorChain next = new RealInterceptorChain(
                interceptors, streamAllocation, tcpStream, connection, index + 1, request,call,eventListener,connectTimeout,sendTimeout);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(next);
        return response;
    }

    public StreamAllocation streamAllocation() {
      return streamAllocation;
    }

    public TcpStream tcpStream(){
        return tcpStream;
    }

    public EventListener eventListener() {
        return eventListener;
    }
}
