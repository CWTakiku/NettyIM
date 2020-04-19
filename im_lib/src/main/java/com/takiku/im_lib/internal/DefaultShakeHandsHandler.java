package com.takiku.im_lib.internal;

import com.takiku.im_lib.internal.handler.ShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class DefaultShakeHandsHandler implements ShakeHandsHandler {


    @Override
    public boolean isShakeHandsOk(Object msg)  {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        if (pack.getShakeHands().getStatusReport()==1&&pack.getShakeHands().getUserId().equals("your userId")){
                 return true;
        }else {
               return false;
        }
    }

    @Override
    public boolean isShakeHands(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.SHAKEHANDS;
    }
}
