package com.takiku.im_lib.util;

public class TimeoutTracker {

    private long timeoutMill;
    private long startTimeMill;
    private long tick;
    private boolean isInfinite;

    private boolean hadStarted;

    public TimeoutTracker(long timeoutMill,long tick){
        if(timeoutMill == -1){
            isInfinite = true;
        }
        this.tick=tick;
        this.timeoutMill = timeoutMill;
    }
    public TimeoutTracker(long timeoutMill){
        if(timeoutMill == -1){
            isInfinite = true;
        }
        this.timeoutMill = timeoutMill;
    }

    public void startTrack(){
        if(hadStarted){
            throw new IllegalStateException("This TimeoutTracker already started");
        }
        hadStarted = true;
        startTimeMill = System.currentTimeMillis();
    }

    public boolean checkTimeout(){
        if(!hadStarted){
            throw new IllegalStateException("This TimeoutTracker haven't started yet");
        }
        if(isInfinite) return false;
        long now = System.currentTimeMillis();;
        return now - startTimeMill > timeoutMill;
    }
    public boolean isTick(){
        long now = System.currentTimeMillis();

        if ((now-startTimeMill)%tick==0){
            return true;
        }else {
            return false;
        }
    }

    public void reset(){
        if(!hadStarted){
            throw new IllegalStateException("This TimeoutTracker haven't started yet");
        }
        startTimeMill = 0;
        hadStarted = false;
    }
}