package com.takiku.im_lib.internal.handler.listener;

/**
 * 消息处理器，一般一种类型消息对应一个消息处理器，开发者可以实现自己的各种消息处理，回调在子线程
 * @param <message>
 */
public interface MessageHandler<message extends com.google.protobuf.GeneratedMessageV3> {
    boolean isFocusMsg(Object msg);
    void handleMsg(message message);
}
