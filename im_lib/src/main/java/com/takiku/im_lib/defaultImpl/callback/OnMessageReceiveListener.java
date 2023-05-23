package com.takiku.im_lib.defaultImpl.callback;

import com.takiku.im_lib.entity.AppMessage;

public interface OnMessageReceiveListener{
        void onMessageReceive(AppMessage appMessage);
    }