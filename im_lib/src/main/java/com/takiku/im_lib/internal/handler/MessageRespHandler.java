package com.takiku.im_lib.internal.handler;

public interface MessageRespHandler<response extends com.google.protobuf.GeneratedMessageV3> {

    boolean isResponse(Object msg); //是否是响应消息
    String tagResponse(response pack);//标记这属于那个request的响应

}
