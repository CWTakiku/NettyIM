package com.takiku.im_lib.internal.handler.internalhandler;

import android.util.Log;

import com.takiku.im_lib.internal.MessageParser;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.listener.EventListener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends ChannelInboundHandlerAdapter {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private RealConnection.connectionBrokenListener connectionBrokenListener;
    private MessageParser messageParser;
    private EventListener eventListener;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker,MessageParser messageParser,
                                  RealConnection.connectionBrokenListener connectionBrokenListener,
                                  EventListener eventListener) {
        this.handshaker = handshaker;
        this.messageParser=messageParser;
        this.eventListener=eventListener;
        this.connectionBrokenListener=connectionBrokenListener;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        eventListener.connectionBroken();
        connectionBrokenListener.connectionBroken();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
           // Print.info("websocket client connected!");
            handshakeFuture.setSuccess();
            eventListener.connectSuccess();
            return;
        }
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            Log.i("WebSocketClientHandler",((TextWebSocketFrame) frame).text());
        }

        if (!messageParser.parseMsg(msg)){

            ctx.fireChannelRead(msg);
        }
//        WebSocketFrame frame = (WebSocketFrame) msg;
//        if (frame instanceof TextWebSocketFrame) {
//
//            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
//            Log.i("WebSocketClientHandler",textFrame.text());
//        } else if (frame instanceof PongWebSocketFrame) {
//        } else if (frame instanceof CloseWebSocketFrame) {
//
//        }

    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        eventListener.connectionException(cause);
        ctx.close();
    }
    public static class Print{
        public static void info(String msg){
            System.out.println(msg);
        }
        public static void error(String msg){
            System.out.println(msg);
        }
    }
}