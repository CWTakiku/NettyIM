package com.takiku.im_lib.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * author:chengwl
 * Description: 接受心跳包回应
 * Date:2020/4/12
 */
public class HeartbeatRespChannelHandler extends ChannelInboundHandlerAdapter {

    private InternalChannelHandler internalChannelHandler;

    public HeartbeatRespChannelHandler(InternalChannelHandler internalChannelHandler){
      this.internalChannelHandler = internalChannelHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       internalChannelHandler.channelRead(ctx,msg);
    }
}
