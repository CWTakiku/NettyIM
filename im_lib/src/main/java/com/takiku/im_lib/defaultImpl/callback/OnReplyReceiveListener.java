package com.takiku.im_lib.defaultImpl.callback;

import com.takiku.im_lib.entity.ReplyMessage;

public interface OnReplyReceiveListener{
        void  onReplyReceive(ReplyMessage replyMessage);
    }