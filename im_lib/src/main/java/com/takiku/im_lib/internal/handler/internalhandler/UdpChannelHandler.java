package com.takiku.im_lib.internal.handler.internalhandler;

import com.takiku.im_lib.internal.MessageParser;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.listener.EventListener;

import java.nio.charset.StandardCharsets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

public class UdpChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private MessageParser messageParser;

    public UdpChannelHandler(MessageParser messageParser){
        this.messageParser=messageParser;


    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
         if (msg== null){
             return;
         }
         String data = msg.content().toString(StandardCharsets.UTF_8);
        System.out.println("收到服务端来信:"+data);
        if (!messageParser.parseMsg(msg)){
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {//DatagramChannel被创建后，会自动触发channelActive
        super.channelActive(ctx);
        if (messageParser.getMessageShakeHandsHandler()!=null){
            if (messageParser.getMessageShakeHandsHandler().ShakeHands() instanceof DatagramPacket){
                ctx.channel().writeAndFlush(((DatagramPacket) messageParser.getMessageShakeHandsHandler().ShakeHands()).retain());
            }else {
                ctx.channel().writeAndFlush(( messageParser.getMessageShakeHandsHandler().ShakeHands()));
            }

        }
    }
}
