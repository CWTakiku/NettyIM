package com.takiku.im_lib.internal.handler;

public interface MessageHandler {

    boolean isResponse(Object msg); //是否是响应消息
    boolean isMessage(Object msg);//是否是其他用户发送过来的消息
    void receiveMessage(Object msg);//接收其他用法发过来的消息

}
