package com.takiku.im_lib.util;

import android.os.CountDownTimer;
import android.os.Looper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CountDownTimerManger {

   private List<Timer>  usingCountDownTimerList=new ArrayList<>();

   private Queue<Timer> freeCountDownTimerQueue=new LinkedList<Timer>();

    private static CountDownTimerManger instance;
    private CountDownTimerManger(){

    }
    public static CountDownTimerManger getInstance(){
        if (instance==null){
            synchronized (CountDownTimerManger.class){
                if (instance==null){
                    instance=new CountDownTimerManger();
                }
            }
        }
        return instance;
    }
    public synchronized Timer getFreeCountDownTimer(long millisInFuture, long countDownInterval){
        if (freeCountDownTimerQueue.size()>0){
            return freeCountDownTimerQueue.poll();
        }else {
            Looper.prepare();
           Timer timer=new Timer(millisInFuture,countDownInterval);


           return timer;
        }
    }
    public synchronized void putCountDownTimer(Timer timer){
        if (freeCountDownTimerQueue.size()<8){
            freeCountDownTimerQueue.add(timer);
        }
    }



}
