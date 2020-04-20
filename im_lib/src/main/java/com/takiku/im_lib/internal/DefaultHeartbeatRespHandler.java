package com.takiku.im_lib.internal;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.internal.handler.HeartbeatRespHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * 心跳包响应处理
 */
public class DefaultHeartbeatRespHandler implements HeartbeatRespHandler {
    public static final int HEART_REPLY_TYPE=0x11;//与服务端保持类型统一

    @Override
    public boolean isHeartbeatResp(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY&&pack.getReply().getReplyType()==HEART_REPLY_TYPE;
    }

    @Override
    public void handleHeartbeatResp(GeneratedMessageV3 heartbeatResp) {

      //TODO 实现心跳包响应处理
    }
}
