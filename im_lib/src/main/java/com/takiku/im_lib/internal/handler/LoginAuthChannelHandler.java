package com.takiku.im_lib.internal.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.exception.AuthError;
import com.takiku.im_lib.exception.AuthException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LoginAuthChannelHandler extends ChannelInboundHandlerAdapter  {
    private com.google.protobuf.GeneratedMessageV3 loginAuth;
    private ShakeHandsHandler shakeHandsHandler;
    private ShakeHandsListener shakeHandsListener;
    public LoginAuthChannelHandler(com.google.protobuf.GeneratedMessageV3 loginAuth, ShakeHandsHandler shakeHandsHandler,
                                   ShakeHandsListener shakeHandsListener){
     this.loginAuth=loginAuth;
     this.shakeHandsHandler = shakeHandsHandler;
     this.shakeHandsListener=shakeHandsListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (loginAuth!=null){
            ctx.channel().writeAndFlush(loginAuth);
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws AuthException {
        if (shakeHandsHandler!=null){
            if (shakeHandsHandler.isShakeHands(msg)){
                if (shakeHandsHandler.isShakeHandsOk((GeneratedMessageV3) msg)){
                    shakeHandsListener.shakeHandsSuccess(true);
                }else {
                    shakeHandsListener.shakeHandsSuccess(false);
                    throw  new AuthException(new AuthError("auth failure"));
                }
            }else {
                ctx.fireChannelRead(msg);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }
  public   interface ShakeHandsListener{
        void shakeHandsSuccess(boolean isSuccess);
    }

}
