package com.takiku.im_lib.internal.handler;

import io.netty.channel.ChannelHandlerContext;

public interface InternalChannelHandler {
    void channelRead(ChannelHandlerContext ctx, Object msg);
}
