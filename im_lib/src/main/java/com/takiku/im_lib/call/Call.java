package com.takiku.im_lib.call;

import com.takiku.im_lib.entity.base.Request;

import java.util.List;

public interface Call extends Cloneable{
    Request request();
    Call enqueue(Callback responseCallback);
    Call clone();
    interface Factory {
        Call newCall(Request request);
    }
    Disposable subscribe(Consumer ... consumers);
    Disposable subscribe(List<Consumer> consumerList);
}
