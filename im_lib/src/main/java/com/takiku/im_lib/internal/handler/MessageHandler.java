package com.takiku.im_lib.internal.handler;

import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    InternalChannelHandler internalChannelHandler;
    com.google.protobuf.Internal.EnumLite commonReply;
    onResponseListener listener;
    public MessageHandler(InternalChannelHandler internalChannelHandler,com.google.protobuf.Internal.EnumLite commonReply,
                          onResponseListener listener ){
        this.internalChannelHandler = internalChannelHandler;
        this.commonReply=commonReply;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        if (pack.getPackType()==commonReply){
            listener.onResponse(msg);
        }
        internalChannelHandler.channelRead(ctx,msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
  public   interface onResponseListener{
      void   onResponse(Object msg);
    }
}
