package com.takiku.im_lib.internal;

import com.takiku.im_lib.internal.handler.ShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * 默认握手实现
 */
public class DefaultShakeHandsHandler implements ShakeHandsHandler {

   public static final int SHAKE_HANDS_REPLY_TYPE=0x12;
    public static final int SHAKE_HANDS_STATUS_SUCCESS=1;
    public static final int SHAKE_HANDS_STATUS_FAILED=0;
    @Override
    public boolean isShakeHandsOk(Object msg)  {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        if (pack.getReply().getStatusReport()== SHAKE_HANDS_STATUS_SUCCESS ){
                 return true;
        }else {
               return false;
        }
    }

    @Override
    public boolean isShakeHands(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY&&pack.getReply().getReplyType()==SHAKE_HANDS_REPLY_TYPE;
    }
}
