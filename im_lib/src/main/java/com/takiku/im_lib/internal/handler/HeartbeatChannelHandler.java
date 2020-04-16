package com.takiku.im_lib.internal.handler;

import com.takiku.im_lib.internal.connection.ConnectionPool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatChannelHandler extends ChannelInboundHandlerAdapter {
    ConnectionPool connectionPool;
    com.google.protobuf.GeneratedMessageV3 heartbeatMsg;
    public HeartbeatChannelHandler(ConnectionPool connectionPool,com.google.protobuf.GeneratedMessageV3 hearBeatMsg){
       this.connectionPool=connectionPool;
       this.heartbeatMsg=hearBeatMsg;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (heartbeatMsg==null){
            return;
        }
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE: {
                    //重连
                    break;
                }

                case WRITER_IDLE: {
                    // 规定时间内没向服务端发送心跳包，即发送一个心跳包
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
              ctx.channel().writeAndFlush(heartbeatMsg);
            }
        }
    }
}
