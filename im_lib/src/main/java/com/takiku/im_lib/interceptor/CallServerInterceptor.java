package com.takiku.im_lib.interceptor;


import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.SendTimeoutException;
import com.takiku.im_lib.internal.connection.IStream;
import com.takiku.im_lib.util.TimeoutTracker;

import java.io.IOException;
import java.util.List;


public class CallServerInterceptor implements Interceptor {

    List<Consumer> consumers;
    TimeoutTracker timer;
    public CallServerInterceptor(List<Consumer> consumers){
        this.consumers=consumers;
    }
    @Override
    public Response intercept(Chain chain) throws IOException, SendTimeoutException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        IStream iStream = realChain.tcpStream();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();
        boolean sendFinish = false;
        Response response = null;
        if (request instanceof ConnectRequest || request.requestBody == null) {
            return new Response.Builder().setRequest(request).setCode(Response.SUCCESS).build();
        }
        if (request.needACK){
            iStream.registerAckConsumer(request);
        }
        if (consumers != null && consumers.size() > 0) {
            iStream.registerConsumers(request, consumers);
        }
        realChain.eventListener().sendMsgStart(realChain.call());
        iStream.writeRequest(request);
        realChain.eventListener().sendMsgEnd(realChain.call());

        if (!request.needACK){
            return new Response.Builder().setRequest(request).setCode(Response.SUCCESS).build();
        }else {
            timer = new TimeoutTracker(realChain.sendTimeoutMillis());
            timer.startTrack();
          //  System.out.println(System.currentTimeMillis());
            while (!timer.checkTimeout()) {
                response = iStream.readResponse(request); //如果规定时间内服务器应答了,收到了发送的消息，则马上注册后续消息状态监听
                if (response != null) {
                    break;
                }
            }
          //  System.out.println(System.currentTimeMillis());
            if (response == null) {
                // throw new  RouteException()
                throw new SendTimeoutException("Service not responding");
            }
            return response;
        }
    }
}
