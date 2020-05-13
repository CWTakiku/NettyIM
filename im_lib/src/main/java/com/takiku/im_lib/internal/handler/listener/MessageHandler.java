package com.takiku.im_lib.internal.handler.listener;

public interface MessageHandler<message extends com.google.protobuf.GeneratedMessageV3> {
    boolean isFocusMsg(Object msg);
    void handleMsg(message message);
}
