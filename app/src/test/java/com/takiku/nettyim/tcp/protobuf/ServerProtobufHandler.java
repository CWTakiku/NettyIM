package com.takiku.nettyim.tcp.protobuf;

import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.MSG_ACK_TYPE;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.MSG_REPLY_TYPE;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.SHAKE_HANDS_ACK_TYPE;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.SHAKE_HANDS_STATUS_FAILED;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.SHAKE_HANDS_STATUS_SUCCESS;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.offLine;
import static com.takiku.nettyim.tcp.protobuf.NettyServerProtobufDemo.userMap;

import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.Session;
import com.takiku.nettyim.SessionManager;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerProtobufHandler extends ChannelInboundHandlerAdapter {

    private final SessionManager sessionManager = SessionManager.getInstance();

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
        PackProtobuf.Pack pack = (PackProtobuf.Pack) msg;
        Session session=   sessionManager.getBySessionId(ctx.channel().id().asLongText());
        switch (pack.getPackType()){
            case SHAKEHANDS:
                PackProtobuf.ShakeHands shakeHands=pack.getShakeHands();
                String userId=shakeHands.getUserId();
                String token=shakeHands.getToken();
                String msgId=shakeHands.getMsgId();
                System.out.println("收到连接认证消息，该用户的id: "+userId+" 该用户的token: "+token );

                if (userMap.containsKey(userId)&&token.equals(userMap.get(userId))){ //连接认证成功
                    ctx.channel().writeAndFlush(createAck(msgId,SHAKE_HANDS_ACK_TYPE,SHAKE_HANDS_STATUS_SUCCESS));

                    Session newSession = Session.buildSession(ctx.channel(),userId);
                    if (session!=null){
                        //TODO 通知别的登录设备下线，这里最好和新的设备比较是否是同一个设备ID，如果一样就不要通知下线，不同的设备才去通知下线
                    }
                    sessionManager.put(newSession.getId(),newSession);
                    sendOfflineMsg(userId);
                }else {
                    ctx.channel().writeAndFlush(createShakeHandsResp(msgId,userId,SHAKE_HANDS_STATUS_FAILED));
                    return;
                }
                break;
            case HEART:
                PackProtobuf.Heart heart=pack.getHeart();

                if (session!=null){
                    System.out.println("收到客户端 "+ session.getUserId());
                    session.writeAndFlush(createAck(heart.getUserId(), NettyServerProtobufDemo.HEART_ACK_TYPE,0));
                }
                break;
            case MSG:
                PackProtobuf.Msg message=pack.getMsg();
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
                    targetSession.writeAndFlush(pack);//转发给接受端
                }else {  //如果对方离线，缓存起来，等用户上线立马发送
                    System.out.println("接受端离线");
                    putOffLienMessage(message.getHead().getToId(),pack);
                }

                break;
            case REPLY:
                PackProtobuf.Reply receiveReply=pack.getReply();
                System.out.println("收到接受方客户端响应的状态:"+receiveReply.toString());
                switch (receiveReply.getReplyType()){
                    case MSG_REPLY_TYPE://消息状态回复，转发给发送方是被送达了，还是被阅读了等
                        System.out.println("转发消息状态给发送方"+receiveReply.getToId());
                        Session replyTargetSession =sessionManager.getByUserId(receiveReply.getToId());
                        if (replyTargetSession!=null) {
                            replyTargetSession.writeAndFlush(pack);
                        }else {//对方离线,消息回执就不要转发了，等用户上线主动来获取消息状态

                        }
                        break;
                }
                break;
        }
    }

    /**
     * 发送离线后的消息
     * @param userId
     */
    private void sendOfflineMsg(String userId) {
        if (offLine.containsKey(userId)){
            Session session=sessionManager.getByUserId(userId);
            if (session==null){
                return;
            }
            List<PackProtobuf.Pack> list=offLine.get(userId);
            List<PackProtobuf.Pack> removeList=new ArrayList<>();
            for (PackProtobuf.Pack pack:list){
                session.writeAndFlush(pack);
                removeList.add(pack);
            }
            list.removeAll(removeList);
        }
    }

    private void putOffLienMessage(String userId, PackProtobuf.Pack pack){
        if (offLine.containsKey(userId)){
            List<PackProtobuf.Pack> list=offLine.get(userId);
            list.add(pack);
        }else {
            List<PackProtobuf.Pack> list=new ArrayList<>();
            list.add(pack);
            offLine.put(userId,list);
        }
    }

    private PackProtobuf.Pack createShakeHandsResp(String msgId,String userId,int status){
       return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.ACK)
                .setAck(PackProtobuf.Ack.newBuilder()
                        .setAckType(SHAKE_HANDS_ACK_TYPE).setAckMsgId(msgId).setResult(status).build())
                .build();
    }
    private PackProtobuf.Pack createMsgReply(String userId,String msgId,int replyType,int status){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply( PackProtobuf.Reply.newBuilder().setToId(userId).setReplyType(replyType).setMsgId(msgId).setStatusReport(status).build())
                .build();
    }
    private PackProtobuf.Pack createAck(String msgId,int ackType,int result){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.ACK)
                .setAck(PackProtobuf.Ack.newBuilder()
                        .setAckType(ackType).setAckMsgId(msgId).setResult(result).build())
                .build();
    }
}