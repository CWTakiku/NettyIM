package com.takiku.im_lib.internal;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * 默认握手实现
 */
public class DefaultMessageShakeHandsHandler implements MessageShakeHandsHandler<PackProtobuf.Pack,PackProtobuf.Pack> {

   public static final int SHAKE_HANDS_REPLY_TYPE=0x12;
    public static final int SHAKE_HANDS_STATUS_SUCCESS=1;
    public static final int SHAKE_HANDS_STATUS_FAILED=0;


    private PackProtobuf.Pack getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("token1");
        shakeHandsMessage.setUserId("user id1");
        shakeHandsMessage.setMsgId("1111");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
    }
    @Override
    public PackProtobuf.Pack ShakeHands() {
        return getDefaultHands();
    }

    @Override
    public boolean isShakeHands(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY
                &&pack.getReply().getReplyType()==SHAKE_HANDS_REPLY_TYPE;
    }

    @Override
    public boolean isShakeHandsOk(PackProtobuf.Pack pack) {
        if (pack.getReply().getStatusReport()== SHAKE_HANDS_STATUS_SUCCESS ){
            return true;
        }else {
            return false;
        }
    }
}
