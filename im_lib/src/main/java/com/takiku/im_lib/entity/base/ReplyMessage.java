package com.takiku.im_lib.entity.base;

import static com.takiku.im_lib.call.Request.PACK_REPLY_TYPE;

public class ReplyMessage extends AbstractPack {
    public ReplyMessage() {
        super(PACK_REPLY_TYPE);
    }
}
