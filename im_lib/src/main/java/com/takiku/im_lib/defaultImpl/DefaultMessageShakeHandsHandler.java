package com.takiku.im_lib.defaultImpl;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * 默认握手实现
 */
public class DefaultMessageShakeHandsHandler implements MessageShakeHandsHandler<PackProtobuf.Pack,PackProtobuf.Pack> {

   public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int SHAKE_HANDS_STATUS_SUCCESS=1;
    public static final int SHAKE_HANDS_STATUS_FAILED=0;

    private PackProtobuf.Pack shakeHands;
    public DefaultMessageShakeHandsHandler(PackProtobuf.Pack shakeHands){
        this.shakeHands=shakeHands;
    }

    @Override
    public PackProtobuf.Pack ShakeHands() {
        return shakeHands;
    }

    @Override
    public boolean isShakeHands(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.ACK
                &&pack.getAck().getAckType()==SHAKE_HANDS_ACK_TYPE;
    }

    @Override
    public boolean isShakeHandsOk(PackProtobuf.Pack pack) {
        if (pack.getAck().getResult()== SHAKE_HANDS_STATUS_SUCCESS ){
            return true;
        }else {
            return false;
        }
    }
}
