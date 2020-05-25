package com.takiku.im_lib.call;

import android.telecom.StatusHints;

public class MessageSubscriber implements Disposable {

    RealCall realCall;
    public MessageSubscriber(RealCall call){
        this.realCall=call;
    }

    @Override
    public void dispose() {
        realCall.dispose();
    }

    @Override
    public boolean isDisposed() {
        return realCall.isDisposed();
    }
}
