package com.takiku.nettyim.websocket.server;


import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.nettyim.Session;
import com.takiku.nettyim.SessionManager;


import java.net.InetSocketAddress;

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
        Session newSession = Session.buildSession(ctx.channel(),req.headers().get("user"));
        if (req.headers().get("user").equals("userid2")){
            System.out.println("2222握手成功");
            channel = ctx.channel();
            if (channel == null){
                System.out.println("2222握手成功 null");
            }
        }
        sessionManager.put(newSession.getId(),newSession);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
          Print.error("数据帧类型不支持!");
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        Session session=   sessionManager.getBySessionId(ctx.channel().id().asLongText());
        // Send the uppercase string back.
        String data = ((TextWebSocketFrame) frame).text();
        System.out.println(data+" "+((InetSocketAddress)ctx.channel().remoteAddress()).getPort());
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;

        JsonObject jsonObject =(JsonObject) new JsonParser().parse(data);
        int type =   jsonObject.get("packType").getAsInt();
        switch (type){

            case  Request.PACK_MSG_TYPE:

                AppMessage message = new Gson().fromJson(data,AppMessage.class);
                System.out.println("收到发送方客户端发送过来的消息:"+message.toString());
                if (session!=null){
                    System.out.println("服务端发送ACK给发送端"+message.getHead().getFromId());
                    session.writeAndFlush(createAck(message.getHead().getMsgId(),MSG_ACK_TYPE, MSG_STATUS_SEND));//服务端发送Ack给发送端
                }else { //发送端已经下线了
                    System.out.println("ACK 回执失败");
                }
                Session targetSession =sessionManager.getByUserId(message.getHead().getToId());
                if (targetSession!=null){ //如果接受端在线
                    String userId = message.getHead().getToId();
                    System.out.println("转发给接受端了"+userId);
                   // channel.writeAndFlush(textWebSocketFrame);
                    targetSession.writeAndFlush(textWebSocketFrame);//转发给接受端
                }else {  //如果对方离线，缓存起来，等用户上线立马发送
                    System.out.println("接受端离线");
                   // putOffLienMessage(message.getHead().getToId(),pack);
                }

                break;
            case Request.PACK_REPLY_TYPE:
                ReplyMessage receiveReply = new Gson().fromJson(data,ReplyMessage.class);
                System.out.println("收到接受方客户端响应的状态:"+receiveReply.toString());
                switch (receiveReply.getReplyType()){
                    case MSG_REPLY_TYPE://消息状态回复，转发给发送方是被送达了，还是被阅读了等
                        System.out.println("转发消息状态给发送方"+receiveReply.getToId());
                        Session replyTargetSession =sessionManager.getByUserId(receiveReply.getToId());
                        if (replyTargetSession!=null) {
                            replyTargetSession.writeAndFlush(frame);
                        }else {//对方离线,消息回执就不要转发了，等用户上线主动来获取消息状态

                        }
                        break;
                }
            case Request.PACK_HEART_TYPE://心跳包
                AckMessage ackMessage = new AckMessage();
                ackMessage.setAckType(HEART_ACK_TYPE);
                TextWebSocketFrame textWebSocketFrame1 = new TextWebSocketFrame(new Gson().toJson(ackMessage));
                ctx.channel().writeAndFlush(textWebSocketFrame1);
                break;
        }
    }
    private TextWebSocketFrame createAck(String msgId,int ackType,int result){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setAckType(ackType);
        ackMessage.setMsgId(msgId);
      return   new TextWebSocketFrame(new Gson().toJson(ackMessage));
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