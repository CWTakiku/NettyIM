package com.takiku.im_lib.internal;

import com.takiku.im_lib.internal.handler.MessageRespHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class DefaultMessageRespHandler implements MessageRespHandler<PackProtobuf.Pack> {
    @Override
    public boolean isResponse(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY;
    }

    @Override
    public String tagResponse(PackProtobuf.Pack pack) {
        return pack.getReply().getMsgId();
    }

}
