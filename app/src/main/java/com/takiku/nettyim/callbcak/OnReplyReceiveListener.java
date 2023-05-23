package com.takiku.nettyim.callbcak;

import com.takiku.im_lib.entity.ReplyMessage;

public interface OnReplyReceiveListener{
        void  onReplyReceive(ReplyMessage replyMessage);
    }