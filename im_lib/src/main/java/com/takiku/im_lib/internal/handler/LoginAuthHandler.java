package com.takiku.im_lib.internal.handler;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.exception.AuthError;
import com.takiku.im_lib.exception.AuthException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LoginAuthHandler extends ChannelInboundHandlerAdapter  {
    private com.google.protobuf.GeneratedMessageV3 loginAuth;
    private ShakeHandsHandler shakeHandsHandler;
    public LoginAuthHandler(com.google.protobuf.GeneratedMessageV3 loginAuth, ShakeHandsHandler shakeHandsHandler){
     this.loginAuth=loginAuth;
     this.shakeHandsHandler = shakeHandsHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (loginAuth!=null){
            ctx.channel().writeAndFlush(loginAuth);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws AuthException {
      if (cause.getCause() instanceof AuthError ){
          throw  new AuthException(new AuthError("auth failure"));
      }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws AuthException {
        if (shakeHandsHandler!=null){
            if (shakeHandsHandler.isShakeHands(msg)){
                if (shakeHandsHandler.isAuthOk(msg)){

                }else {
                    throw  new AuthException(new AuthError("auth failure"));
                }
            }else {
                ctx.fireChannelRead(msg);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }

}
