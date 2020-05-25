package com.takiku.nettyim.callbcak;


import android.os.Handler;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Consumer;

public class UIConsumerCallback<T extends com.google.protobuf.GeneratedMessageV3> implements Consumer<T> {
    private android.os.Handler mDeliveryHandler;
    private Consumer consumer;
    public UIConsumerCallback(Consumer<T> consumer, Handler handler){
        this.consumer=consumer;
        this.mDeliveryHandler=handler;
    }

    @Override
    public void accept(final T t) {
         mDeliveryHandler.post(new Runnable() {
             @Override
             public void run() {
                 consumer.accept(t);
             }
         });
    }

    @Override
    public boolean Observable(T t, String requestTag) {
     return   consumer.Observable(t,requestTag);
    }
}
