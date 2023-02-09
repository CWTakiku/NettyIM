package com.takiku.im_lib.internal.handler.internalhandler;

import com.takiku.im_lib.exception.AuthError;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.util.LogUtil;

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
        super.channelActive(ctx);
        eventListener.connectSuccess();
        LogUtil.i("StatusChannelHandler","tcp  connect!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws AuthException {
        eventListener.connectionException(cause);
        if (cause.getCause() instanceof AuthError){
            throw  new AuthException(new AuthError("auth failure"));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        connectionBrokenListener.connectionBroken();
        eventListener.connectionBroken();
        LogUtil.i("StatusChannelHandler","tcp  disconnect!");

    }
}
