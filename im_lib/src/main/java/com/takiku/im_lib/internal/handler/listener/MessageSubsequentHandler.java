package com.takiku.im_lib.internal.handler.listener;

public interface MessageSubsequentHandler<response extends com.google.protobuf.GeneratedMessageV3> {
    /**
     * 是否是服务端响应消息
     * @param msg
     * @return
     */
    boolean isSubsequent(response msg); //是否是该消息后续的回应包，例如该消息的已读、撤回等

    /**
     * 服务端响应的tag对应发送哪条消息
     * @param pack
     * @return
     */
    String messageTag(response pack);//标记这回应包属于那个request的,是哪个消息的

}
