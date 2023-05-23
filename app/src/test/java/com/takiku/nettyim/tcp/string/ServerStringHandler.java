package com.takiku.nettyim.tcp.string;

import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.SHAKE_HANDS_STATUS_SUCCESS;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.offLine;
import static com.takiku.nettyim.tcp.string.NettyServerStringDemo.userMap;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.Session;
import com.takiku.nettyim.SessionManager;
import com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo;
import com.takiku.nettyim.tcp.protobuf.ServerProtobufHandler;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author chengwl
 * @des
 * @date:2023/5/23
 */
public class ServerStringHandler extends ChannelInboundHandlerAdapter {

    private final SessionManager sessionManager = SessionManager.getInstance();

    public static final int MSG_REPLY_TYPE=0x10;
    public static final int HEART_ACK_TYPE=0x11;
    public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int MSG_ACK_TYPE=0x13;

    private static final String TAG = ServerProtobufHandler.class.getSimpleName();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("ServerHandler channelActive()" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String sessionId = Session.buildId(ctx.channel());
        Session session = sessionManager.removeBySessionId(sessionId);
        System.out.println("ServerHandler channelInactive() userId "+session.getUserId());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("ServerHandler exceptionCaught()");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        System.out.println("ServerHandler userEventTriggered()");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JsonObject jsonObject =(JsonObject) new JsonParser().parse((String) msg);
        int type =   jsonObject.get("packType").getAsInt();
        Session session=   sessionManager.getBySessionId(ctx.channel().id().asLongText());
        switch (type){
            case Request.PACK_HANDS_TYPE:
                ShakeHandsMessage shakeHands = new Gson().fromJson((String) msg,ShakeHandsMessage.class);
                String userId=shakeHands.getUserId();
                String token=shakeHands.getToken();
                String msgId=shakeHands.getMsgId();
                System.out.println("收到连接认证消息，该用户的id: "+userId+" 该用户的token: "+token );

                if (userMap.containsKey(userId)&&token.equals(userMap.get(userId))){ //连接认证成功
                    ctx.channel().writeAndFlush(createAck(msgId,SHAKE_HANDS_ACK_TYPE,SHAKE_HANDS_STATUS_SUCCESS));
                    System.out.println("认证成功 "+userId+" 该用户的token: "+token );
                    Session newSession = Session.buildSession(ctx.channel(),userId);
                    if (session!=null){
                        //TODO 通知别的登录设备下线，这里最好和新的设备比较是否是同一个设备ID，如果一样就不要通知下线，不同的设备才去通知下线
                    }
                    sessionManager.put(newSession.getId(),newSession);
                   // sendOfflineMsg(userId);
                }else {
                  //  ctx.channel().writeAndFlush(createShakeHandsResp(msgId,userId,SHAKE_HANDS_STATUS_FAILED));
                    return;
                }
                break;
            case Request.PACK_HEART_TYPE:

                if (session!=null){
                    System.out.println("收到客户端 "+ session.getUserId());
                    AckMessage ackMessage = new AckMessage();
                    ackMessage.setAckType(HEART_ACK_TYPE);
                    ctx.channel().writeAndFlush(new Gson().toJson(ackMessage));
                }
                break;
            case Request.PACK_MSG_TYPE:
                AppMessage message = new Gson().fromJson((String) msg,AppMessage.class);
                System.out.println("收到发送方客户端发送过来的消息:"+message.toString());
                if (session!=null){
                    System.out.println("服务端发送ACK给发送端");
                    session.writeAndFlush(createAck(message.getHead().getMsgId(),MSG_ACK_TYPE, NettyServerProtobufDemo.MSG_STATUS_SEND));//服务端发送Ack给发送端
                }else { //发送端已经下线了
                    System.out.println("ACK 回执失败");
                }
                Session targetSession =sessionManager.getByUserId(message.getHead().getToId());
                if (targetSession!=null){ //如果接受端在线
                    System.out.println("转发给接受端了");
                    targetSession.writeAndFlush(new Gson().toJson(message));//转发给接受端
                }else {  //如果对方离线，缓存起来，等用户上线立马发送
                    System.out.println("接受端离线");
                  //  putOffLienMessage(message.getHead().getToId(),pack);
                }

                break;
            case Request.PACK_REPLY_TYPE:
                ReplyMessage receiveReply = new Gson().fromJson((String) msg,ReplyMessage.class);
                System.out.println("收到接受方客户端响应的状态:"+receiveReply.toString());
                switch (receiveReply.getReplyType()){
                    case MSG_REPLY_TYPE://消息状态回复，转发给发送方是被送达了，还是被阅读了等
                        System.out.println("转发消息状态给发送方"+receiveReply.getToId());
                        Session replyTargetSession =sessionManager.getByUserId(receiveReply.getToId());
                        if (replyTargetSession!=null) {
                            replyTargetSession.writeAndFlush(new Gson().toJson(receiveReply));
                        }else {//对方离线,消息回执就不要转发了，等用户上线主动来获取消息状态

                        }
                        break;
                }
                break;
        }
    }






    private String createAck(String msgId,int ackType,int result){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setAckType(ackType);
        ackMessage.setMsgId(msgId);
        return   new Gson().toJson(ackMessage);
    }
}
