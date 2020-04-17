package com.takiku.im_lib.internal.handler;

import com.takiku.im_lib.exception.AuthError;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.listener.EventListener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class StatusChannelHandler extends ChannelInboundHandlerAdapter {
    private EventListener eventListener;
    private RealConnection.connectionBrokenListener connectionBrokenListener;
    public StatusChannelHandler(EventListener eventListener, RealConnection.connectionBrokenListener connectionBrokenListener ){
        this.eventListener=eventListener;
        this.connectionBrokenListener=connectionBrokenListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        eventListener.connectSuccess();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws AuthException {
        System.out.println("exceptionCaught  "+cause.getMessage());
        if (cause.getCause() instanceof AuthError){
            throw  new AuthException(new AuthError("auth failure"));
        }
        eventListener.connectionException(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("channelInactive");
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
        connectionBrokenListener.connectionBroken();
        eventListener.connectionBroken();

    }
}
