package com.takiku.im_lib.interceptor;


import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.internal.connection.TcpStream;
import com.takiku.im_lib.util.TimeoutTracker;

import java.io.IOException;

import static com.takiku.im_lib.entity.base.Response.NO_RESPONSE;


public class CallServerInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        TcpStream tcpStream=realChain.tcpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();
        realChain.eventListener().sendMsgStart(realChain.call());
        long sentRequestMillis = System.currentTimeMillis();
        boolean sendFinish=false;
        Response response=null;

        if (request.body==null){
            return new Response.Builder().setRequest(request).build();
        }

        tcpStream.writeRequest(request);
        realChain.eventListener().sendMsgEnd(realChain.call());
        TimeoutTracker timer=new TimeoutTracker(realChain.sendTimeoutMillis());
        timer.startTrack();
        while (!timer.checkTimeout()){
            response  =tcpStream.readResponse(request);
            if (response!=null){
                break;
            }
        }
       if (response==null){
           // throw new  RouteException()
            return new Response.Builder().setCode(NO_RESPONSE).setRequest(request).build();
        }
        return response;
    }
}
