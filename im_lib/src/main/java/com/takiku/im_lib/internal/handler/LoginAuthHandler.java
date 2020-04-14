package com.takiku.im_lib.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LoginAuthHandler extends ChannelInboundHandlerAdapter {
    private com.google.protobuf.Internal.EnumLite loginAuth;
    private InternalChannelHandler internalChannelHandler;
    public LoginAuthHandler(com.google.protobuf.Internal.EnumLite loginAuth, InternalChannelHandler internalChannelHandler){
     this.loginAuth=loginAuth;
     this.internalChannelHandler = internalChannelHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
          ctx.channel().writeAndFlush(loginAuth);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        internalChannelHandler.channelRead(ctx,msg);
    }
}
