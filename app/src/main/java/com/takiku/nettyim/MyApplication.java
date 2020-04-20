package com.takiku.nettyim;

import android.app.Application;
import android.content.Context;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.internal.DefaultMessageRespHandler;
import com.takiku.im_lib.internal.DefaultShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class MyApplication extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }


}
