package com.takiku.im_lib.internal.handler;

/**
 * author:chengwl
 * Description:消息接收，来自其他客户端的消息
 * Date:2020/4/18
 */
public interface MessageReceiveHandler<message extends com.google.protobuf.GeneratedMessageV3> {
    /**
     * 是否是用户发的消息
     * @param msg
     * @return
     */
    boolean isClientMessage(Object msg);

    /**
     * 接受用户发送的消息
     * @param message
     */
    void receiveMessage(message message);

}
