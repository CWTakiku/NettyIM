package com.takiku.im_lib.server;


import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpUtil.isOriginForm;


import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;


public class MyWebSocketServerHandler extends ChannelInboundHandlerAdapter {
    private static final String WEBSOCKET_PATH = "";
    private WebSocketServerHandshaker handshaker;
    private static final String HOST = "";
    private final SessionManager sessionManager = SessionManager.getInstance();
    public static final int MSG_REPLY_TYPE=0x10;
    public static final int HEART_ACK_TYPE=0x11;
    public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int MSG_ACK_TYPE=0x13;

    public static final int MSG_STATUS_SEND=1;//发送成功
    public static final int MSG_STATUS_READ=2;
    private Channel channel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //以http请求形式接入，但是走的是websocket
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            //处理websocket客户端的消息
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //要求Upgrade为websocket，过滤掉get/Post


        System.out.println(req.headers().get("user"));
        if (!req.decoderResult().isSuccess()
                || (!"websocket".equals(req.headers().get("Upgrade")))) {
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:8804/websocket", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
        System.out.println(req.headers().get("user")+"握手成功");


        Session newSession = Session.buildSession(ctx.channel(), new Random().nextInt(100)+"");
//        if (req.headers().get("user").equals("userid2")){
//            System.out.println("2222握手成功");
//            channel = ctx.channel();
//            if (channel == null){
//                System.out.println("2222握手成功 null");
//            }
//        }
        sessionManager.put(newSession.getId(),newSession);
    }
    private void handleWebSocketFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame msg){
        Print.info("服务器接收到二进制消息,消息长度:[{}]"+ msg.content().capacity());
        ByteBuf byteBuf = Unpooled.directBuffer(msg.content().capacity());
        byteBuf.writeBytes(msg.content());
        Print.info(byteBuf.capacity()+" ");
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        Session session=   sessionManager.getBySessionId(ctx.channel().id().asLongText());
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof BinaryWebSocketFrame){
            Print.info("服务器接收到二进制消息,消息长度:[{}]"+ frame.content().capacity());
         //   ByteBuf byteBuf = Unpooled.directBuffer(frame.content().capacity());
            int type = frame.content().getInt(32);
            Print.info("type "+type);
            if (type == 1){
                if (SessionManager.getInstance().getSessionIdMap()!=null){
                    for (Session session1:SessionManager.getInstance().getSessionIdMap().values()){
                        if (!session.getUserId().equals(session1.getUserId())){
                            System.out.println("转发字节给用户 "+session1.getUserId());
                            session1.writeAndFlush(frame);
                        }
                    }
                }
            }
            return;
        }
        if ((frame instanceof TextWebSocketFrame)) {
            String data = ((TextWebSocketFrame) frame).text();
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
            System.out.println(data);
            if (SessionManager.getInstance().getSessionIdMap()!=null){
                for (Session session1:SessionManager.getInstance().getSessionIdMap().values()){
                    if (!session.getUserId().equals(session1.getUserId())){
                        System.out.println("转发给用户 "+session1.getUserId());
                        session1.writeAndFlush(textWebSocketFrame);
                    }
                }
            }
         return;
        }
        throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));

        // Send the uppercase string back.


    }


    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res); // 如果是非Keep-Alive，关闭连接
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }

    /**
     * 接收客户端连接事件
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Print.info("客户端与服务端连接开启：" + ctx.channel());


        ctx.channel().writeAndFlush(new TextWebSocketFrame("你好我来自服务端"));
    }


    /**
     * 接收客户端关闭事件
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Print.info("客户端与服务端连接关闭：" + ctx.channel());
        String sessionId = Session.buildId(ctx.channel());
        Session session = sessionManager.removeBySessionId(sessionId);
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