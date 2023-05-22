package com.takiku.nettyim.callbcak;

import com.takiku.im_lib.entity.AppMessage;

public interface OnMessageReceiveListener{
        void onMessageReceive(AppMessage appMessage);
    }