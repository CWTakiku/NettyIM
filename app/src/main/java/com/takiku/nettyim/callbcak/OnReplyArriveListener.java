package com.takiku.nettyim.callbcak;

import com.takiku.im_lib.entity.ReplyMessage;

public interface OnReplyArriveListener{
        void onReplyArrive(ReplyMessage replyMessage);
}