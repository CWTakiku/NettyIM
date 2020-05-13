package com.takiku.im_lib.internal;

import com.takiku.im_lib.internal.handler.listener.MessageSubsequentHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * 默认消息响应处理，服务端响应我们发送的消息状态，已读，撤回，送达等消息状态，
 * tagResponse 返回发送消息的那个tag，后续所有服务端的该消息的状态都会统一回调
 */
public class DefaultMessageSubsequentHandler implements MessageSubsequentHandler<PackProtobuf.Pack> {
    public static final int MSG_REPLY_TYPE=0x10;


    @Override
    public boolean isSubsequent(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY&&pack.getReply().getReplyType()==MSG_REPLY_TYPE;
    }

    @Override
    public String messageTag(PackProtobuf.Pack pack) {
        return pack.getReply().getMsgId();
    }
}
