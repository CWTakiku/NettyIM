package com.takiku.im_lib.entity;

import com.takiku.im_lib.entity.base.AbstractPack;

import static com.takiku.im_lib.entity.base.Request.PACK_REPLY_TYPE;

public class ReplyMessage extends AbstractPack {
    public ReplyMessage() {
        super(PACK_REPLY_TYPE);
    }
}
