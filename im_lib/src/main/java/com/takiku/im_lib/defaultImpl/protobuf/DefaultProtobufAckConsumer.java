package com.takiku.im_lib.defaultImpl.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class DefaultProtobufAckConsumer implements Consumer<PackProtobuf.Pack> {
    public static final int MSG_ACK_TYPE=0x13;

    @Override
    public boolean Observable(PackProtobuf.Pack pack, String requestTag) {
        return pack.getPackType()==PackProtobuf.Pack.PackType.ACK
                &&pack.getAck().getAckType()==MSG_ACK_TYPE
                &&pack.getAck().getAckMsgId().equals(requestTag);
    }

    @Override
    public void accept(PackProtobuf.Pack pack) {

    }
}
