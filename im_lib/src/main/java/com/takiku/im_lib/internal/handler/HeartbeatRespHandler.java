package com.takiku.im_lib.internal.handler;

public interface HeartbeatRespHandler<response extends com.google.protobuf.GeneratedMessageV3> {
   boolean isHeartbeatResp(Object msg);//是否是心跳回应包
   void  handleHeartbeatResp(response heartbeatResp);//处理心跳回应包
}
