package com.takiku.im_lib.interceptor;


import android.os.Handler;
import android.os.Looper;

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

        tcpStream.writeRequest(request);
        realChain.eventListener().sendMsgEnd(realChain.call());
                Timer timer= CountDownTimerManger.getInstance().getFreeCountDownTimer(realChain.sendTimeoutMillis(),100);
                timer.startCountDown(new Timer.countDownListener() {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        System.out.println("onTick "+millisUntilFinished);
                        response[0] =tcpStream.readResponse(request);
                        if (response[0] !=null){
                            timer.cancel();
                            CountDownTimerManger.getInstance().putCountDownTimer(timer);

                        }
                    }

                    @Override
                    public void onFinish() {
                        timer.cancel();
                        timer.release();
                        CountDownTimerManger.getInstance().putCountDownTimer(timer);

                    }
                });

        if (response[0] !=null){
            response[0].setRequest(request);
        }
        return response[0];
    }
}
