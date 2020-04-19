package com.takiku.im_lib.internal.handler;

/**
 * author:chengwl
 * Description:消息接收，来自其他客户端的消息
 * Date:2020/4/18
 */
public interface MessageReceiveHandler<message extends com.google.protobuf.GeneratedMessageV3> {
    boolean isClientMessage(Object msg);
    void receiveMessage(message message);

}
