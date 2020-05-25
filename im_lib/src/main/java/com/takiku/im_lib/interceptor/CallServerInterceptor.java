package com.takiku.im_lib.interceptor;


import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.SendTimeoutException;
import com.takiku.im_lib.internal.connection.TcpStream;
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
        TcpStream tcpStream = realChain.tcpStream();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();
        boolean sendFinish = false;
        Response response = null;
        if (request instanceof ConnectRequest || request.requestBody == null) {
            return new Response.Builder().setRequest(request).setCode(Response.SUCCESS).build();
        }
        if (request.needACK){
            tcpStream.registerAckConsumer(request);
        }
        realChain.eventListener().sendMsgStart(realChain.call());
        tcpStream.writeRequest(request);
        realChain.eventListener().sendMsgEnd(realChain.call());
        if (consumers != null && consumers.size() > 0) {
            tcpStream.registerConsumers(request, consumers);
        }
        if (!request.needACK){
            return new Response.Builder().setRequest(request).setCode(Response.SUCCESS).build();
        }else {
            timer = new TimeoutTracker(realChain.sendTimeoutMillis());
            timer.startTrack();
            while (!timer.checkTimeout()) {
                response = tcpStream.readResponse(request); //如果规定时间内服务器应答了,收到了发送的消息，则马上注册后续消息状态监听
                if (response != null) {
                    break;
                }
            }
            if (response == null) {
                // throw new  RouteException()
                throw new SendTimeoutException("Service not responding");
            }
            return response;
        }
    }
}
