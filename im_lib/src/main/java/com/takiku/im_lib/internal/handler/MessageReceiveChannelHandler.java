package com.takiku.im_lib.internal.handler;

import com.google.protobuf.GeneratedMessageV3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * author:chengwl
 * Description:消息接收,面向其他客户端发送的消息
 * Date:2020/4/18
 */
public class MessageReceiveChannelHandler extends ChannelInboundHandlerAdapter {

    private MessageReceiveHandler messageReceiveHandler;
    public MessageReceiveChannelHandler(MessageReceiveHandler messageReceiveHandler){
        this.messageReceiveHandler=messageReceiveHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (messageReceiveHandler!=null){
            if (messageReceiveHandler.isClientMessage(msg)){
                 messageReceiveHandler.receiveMessage((GeneratedMessageV3) msg);
            }else {
                ctx.fireChannelRead(msg);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }
}
