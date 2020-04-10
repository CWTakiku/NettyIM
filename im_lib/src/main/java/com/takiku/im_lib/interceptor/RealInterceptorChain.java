package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.Codec.Codec;
import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.dispatcher.RealConnection;
import com.takiku.im_lib.dispatcher.StreamAllocation;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;
import java.util.List;

public class RealInterceptorChain implements Interceptor.Chain {
    private final Request request;
    private final List<Interceptor> interceptors;
    private final int index;
    private final Codec codec;
    private final StreamAllocation streamAllocation;
    private final RealConnection realConnection;
    public RealInterceptorChain(List<Interceptor> interceptors, StreamAllocation streamAllocation, Codec codec, RealConnection realConnection, int index, Request request){
        this.request=request;
        this.index=index;
        this.interceptors=interceptors;
        this.codec=codec;
        this.streamAllocation=streamAllocation;
        this.realConnection=realConnection;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        return null;
    }
}
