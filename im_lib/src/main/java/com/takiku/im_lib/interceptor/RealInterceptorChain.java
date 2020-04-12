package com.takiku.im_lib.interceptor;

import androidx.annotation.Nullable;

import com.takiku.im_lib.Codec.Codec;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RealInterceptorChain implements Interceptor.Chain {
    private final Request request;
    private final List<Interceptor> interceptors;
    private final int index;
    private final Codec codec;
    private final StreamAllocation streamAllocation;
    private final RealConnection connection;
    private final int connectTimeout;
    private final int sendTimeout;
    public RealInterceptorChain(List<Interceptor> interceptors,
                                StreamAllocation streamAllocation,
                                Codec codec, RealConnection realConnection,
                                int index, Request request, Call call, EventListener eventListener,
                                int connectTimeout,int sendTimeout){
        this.request=request;
        this.index=index;
        this.interceptors=interceptors;
        this.codec=codec;
        this.streamAllocation=streamAllocation;
        this.connection=realConnection;
        this.connectTimeout=connectTimeout;
        this.sendTimeout=sendTimeout;
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
    public Response proceed(Request request) throws IOException {
        return proceed(request, streamAllocation, codec, connection);
    }

    @Override
    public Call call() {
        return null;
    }

    public Response proceed(Request request, StreamAllocation streamAllocation, Codec codec,
                            RealConnection connection) throws IOException {

    }

    public StreamAllocation streamAllocation() {
      return streamAllocation;
    }
}
