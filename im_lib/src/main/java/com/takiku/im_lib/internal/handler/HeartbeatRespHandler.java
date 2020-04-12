package com.takiku.im_lib.internal.handler;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * author:chengwl
 * Description: 接受心跳包回应
 * Date:2020/4/12
 */
public class HeartbeatRespHandler extends ChannelInboundHandlerAdapter {
    com.google.protobuf.Internal.EnumLite enumLite;

    public HeartbeatRespHandler(com.google.protobuf.Internal.EnumLite heartEnumLite){
        this.enumLite=heartEnumLite;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        if (pack.getPackType()==enumLite){
            System.out.println("收到服务端心跳响应消息，message=" + pack.getHeart().toString());
        }else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }

    }
}
