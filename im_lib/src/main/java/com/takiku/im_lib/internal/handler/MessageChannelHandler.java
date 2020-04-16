package com.takiku.im_lib.internal.handler;



import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageChannelHandler extends ChannelInboundHandlerAdapter {

    MessageHandler messageHandler;
    onResponseListener listener;
    public MessageChannelHandler(MessageHandler messageHandler,
                                 onResponseListener listener ){
        this.messageHandler = messageHandler;
        this.listener=listener;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
         if (messageHandler.isResponse(msg)){
             listener.onResponse(msg);
         }else if (messageHandler.isMessage(msg)){
             messageHandler.receiveMessage(msg);
         }else {
             ctx.fireChannelRead(msg);
         }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       System.out.println(cause.getMessage());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("IMClient channelInactive");
    }
  public   interface onResponseListener{
      void   onResponse(Object msg);
    }
}
