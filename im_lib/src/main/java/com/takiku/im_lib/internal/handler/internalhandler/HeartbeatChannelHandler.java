package com.takiku.im_lib.internal.handler.internalhandler;

import android.util.Log;

import com.takiku.im_lib.internal.connection.ConnectionPool;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.im_lib.util.LogUtil;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatChannelHandler extends ChannelInboundHandlerAdapter {
    ConnectionPool connectionPool;
    Object heartbeatMsg;
    RealConnection.connectionBrokenListener connectionBrokenListener;
    @IMProtocol int protocol;
    public HeartbeatChannelHandler(@IMProtocol int protocol, ConnectionPool connectionPool, Object hearBeatMsg, RealConnection.connectionBrokenListener connectionBrokenListener){
       this.connectionPool=connectionPool;
       this.heartbeatMsg=hearBeatMsg;
       this.connectionBrokenListener=connectionBrokenListener;
       this.protocol = protocol;
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        LogUtil.i("HeartbeatChannelHandler","userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE: {
                    LogUtil.i("HeartbeatChannelHandler", "userEventTriggered:READER_IDLE ");
                   // connectionBrokenListener.connectionBroken();
                    break;
                }
                case WRITER_IDLE: {
                    LogUtil.i("HeartbeatChannelHandler", "userEventTriggered:WRITER_IDLE ");
                    // 规定时间内没向服务端发送心跳包,则马上发送一个心跳包
                    if (heartbeatTask == null) {
                        heartbeatTask = new HeartbeatTask(ctx);
                    }
                    connectionPool.execWorkTask(heartbeatTask);
                    break;
                }
            }
        }
    }
    private HeartbeatTask heartbeatTask;
    private class HeartbeatTask implements Runnable {

        private ChannelHandlerContext ctx;

        public HeartbeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (ctx.channel().isActive()) {
                if (heartbeatMsg instanceof ByteBufHolder){
                    ctx.channel().writeAndFlush(((ByteBufHolder) heartbeatMsg).retain());
                }else {
                    ctx.channel().writeAndFlush((heartbeatMsg));
                }
            }
        }
    }
}
