package com.takiku.im_lib.util;

import android.os.CountDownTimer;
import android.os.Looper;

public class Timer extends CountDownTimer {


    private countDownListener countDownListener;
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (countDownListener!=null){
                countDownListener.onTick(millisUntilFinished);
            }
        }

        @Override
        public void onFinish() {
            if (countDownListener!=null){
                countDownListener.onFinish();
            }
        }
        public interface countDownListener{
            void onTick(long millisUntilFinished);
            public void onFinish();
        }

        public void startCountDown(countDownListener countDownListener){
            this.countDownListener=countDownListener;
            start();
            Looper.loop();
        }
        public void release(){
            Looper.myLooper().quit();
        }
}
