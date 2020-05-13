package com.takiku.im_lib.internal.handler.internalhandler;

import com.takiku.im_lib.internal.MessageParser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageChannelHandler extends ChannelInboundHandlerAdapter {

    private MessageParser messageParser;
    public MessageChannelHandler(MessageParser messageParser){
        this.messageParser=messageParser;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (messageParser.getMessageShakeHandsHandler()!=null){
            ctx.channel().writeAndFlush(messageParser.getMessageShakeHandsHandler().ShakeHands());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg==null){
            return;
        }
        if (!messageParser.parseMsg(msg)){
            ctx.fireChannelRead(msg);
        }
    }
}
