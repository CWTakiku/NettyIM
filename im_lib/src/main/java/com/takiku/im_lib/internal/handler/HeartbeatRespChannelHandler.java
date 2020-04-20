package com.takiku.im_lib.internal.handler;

import com.google.protobuf.GeneratedMessageV3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * author:chengwl
 * Description: 接受心跳包回应
 * Date:2020/4/12
 */
public class HeartbeatRespChannelHandler extends ChannelInboundHandlerAdapter {

    private HeartbeatRespHandler heartbeatRespHandler;

    public HeartbeatRespChannelHandler(HeartbeatRespHandler heartbeatRespHandler){
      this.heartbeatRespHandler = heartbeatRespHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (heartbeatRespHandler!=null){
            if (heartbeatRespHandler.isHeartbeatResp(msg)){
                heartbeatRespHandler.handleHeartbeatResp((GeneratedMessageV3) msg);
            }else {
                ctx.fireChannelRead(msg);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }
}
