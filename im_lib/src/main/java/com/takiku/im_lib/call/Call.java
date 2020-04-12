package com.takiku.im_lib.call;

public interface Call extends Cloneable{
    Request request();
    void enqueue(Callback responseCallback);
    Call clone();
    interface Factory {
        Call newCall(Request request);
    }
}
