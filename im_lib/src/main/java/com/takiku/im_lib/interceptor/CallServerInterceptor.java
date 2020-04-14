package com.takiku.im_lib.interceptor;


import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.internal.connection.TcpStream;
import com.takiku.im_lib.util.CountDownTimerManger;
import com.takiku.im_lib.util.Timer;

import java.io.IOException;


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
        final Response[] response = {null};
        final boolean[] result = {false};
        while (!result[0]){
            if (!sendFinish){
                Timer timer= CountDownTimerManger.getInstance().getFreeCountDownTimer(realChain.sendTimeoutMillis(),100);
                timer.startCountDown(new Timer.countDownListener() {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        response[0] =tcpStream.readResponse(request);
                        if (response[0] !=null){
                            timer.cancel();
                            CountDownTimerManger.getInstance().putCountDownTimer(timer);
                           result[0] =true;
                        }
                    }

                    @Override
                    public void onFinish() {
                        result[0] =true;
                        CountDownTimerManger.getInstance().putCountDownTimer(timer);
                    }
                });
                tcpStream.writeRequest(request);
                realChain.eventListener().sendMsgEnd(realChain.call());
            }
            sendFinish=true;
        }

        if (response[0] !=null){
            response[0].setRequest(request);
        }
        return response[0];
    }
}
