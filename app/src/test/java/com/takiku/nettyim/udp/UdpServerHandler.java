package com.takiku.nettyim.udp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.nettyim.Session;
import com.takiku.nettyim.SessionManager;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/16
 */
public class UdpServerHandler  extends SimpleChannelInboundHandler<DatagramPacket> {

    HashMap<String, InetSocketAddress> inetSocketAddressHashMap = new HashMap<>();
    public static final int MSG_REPLY_TYPE=0x10;
    public static final int HEART_ACK_TYPE=0x11;
    public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int MSG_ACK_TYPE=0x13;

    public static final int MSG_STATUS_SEND=1;//发送成功
    public static final int MSG_STATUS_READ=2;
    private final SessionManager sessionManager = SessionManager.getInstance();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String data =msg.content().toString(StandardCharsets.UTF_8);


        System.out.println(data);

        Session session=   sessionManager.getBySessionId(ctx.channel().id().asLongText());
        JsonObject jsonObject =(JsonObject) new JsonParser().parse(data);
        int type =   jsonObject.get("packType").getAsInt();
        switch (type){

            case Request.PACK_HANDS_TYPE:
                System.out.println("收到发送方客户端发送过来的握手消息:");//开始鉴权
                ShakeHandsMessage shakeHandsMessage = new Gson().fromJson(data,ShakeHandsMessage.class);
                if (!inetSocketAddressHashMap.containsKey(shakeHandsMessage.getUserId())){
                    inetSocketAddressHashMap.put(shakeHandsMessage.getUserId(),msg.sender());
                    Session newSession = Session.buildSession(ctx.channel(),shakeHandsMessage.getUserId());
                    sessionManager.put(newSession.getId(),newSession);

                    if (shakeHandsMessage.getToken().equals("token"+"userid1")||shakeHandsMessage.getToken().equals("token"+"userid2")){
                        System.out.println("鉴权成功");//鉴权成功
                        AckMessage ackMessage = new AckMessage();
                        ackMessage.setAckType(SHAKE_HANDS_ACK_TYPE);
                        DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer((String)new Gson().toJson(ackMessage), Charset.forName("UTF-8")),msg.sender());
                        newSession.getChannel().writeAndFlush(datagramPacket);
                    }

                }
                break;

            case  Request.PACK_MSG_TYPE:

                AppMessage message = new Gson().fromJson(data,AppMessage.class);
                System.out.println("收到发送方客户端发送过来的消息:"+data);
                if (session!=null){
                    System.out.println("服务端发送ACK给发送端"+message.getHead().getFromId());
                    DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer(createAck(message.getHead().getMsgId(),MSG_ACK_TYPE, MSG_STATUS_SEND),Charset.forName("UTF-8")),msg.sender());
                    session.writeAndFlush(datagramPacket);//服务端发送Ack给发送端
                }else { //发送端已经下线了
                    System.out.println("ACK 回执失败");
                }
                Session targetSession =sessionManager.getByUserId(message.getHead().getToId());
                if (targetSession!=null){ //如果接受端在线
                    String userId = message.getHead().getToId();
                    System.out.println("转发给接受端了"+userId);
                    DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer((String)msg.content().toString(StandardCharsets.UTF_8), Charset.forName("UTF-8")),inetSocketAddressHashMap.get(userId));
                    targetSession.getChannel().writeAndFlush(datagramPacket);//转发给相应的客户端
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
                            DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer((String)msg.content().toString(StandardCharsets.UTF_8), Charset.forName("UTF-8")),inetSocketAddressHashMap.get(receiveReply.getToId()));
                            replyTargetSession.getChannel().writeAndFlush(datagramPacket);
                        }else {//对方离线,消息回执就不要转发了，等用户上线主动来获取消息状态

                        }
                        break;
                }
            case Request.PACK_HEART_TYPE://心跳包
                System.out.println("接受到心跳包");
                AckMessage ackMessage = new AckMessage();
                ackMessage.setAckType(HEART_ACK_TYPE);
                DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer(new Gson().toJson(ackMessage), Charset.forName("UTF-8")),msg.sender());
                ctx.channel().writeAndFlush(datagramPacket);
                break;
        }


        System.out.println(msg.sender().getAddress().getHostAddress()+":"+msg.sender().getPort());
        System.out.println("收到啦！"+msg.content().toString(StandardCharsets.UTF_8)+" "+msg.sender().getAddress().getHostAddress()+" ");
//         DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer((String)"知道啦", Charset.forName("UTF-8")),msg.sender());
//        ctx.channel().writeAndFlush(datagramPacket);

    }
    private String createAck(String msgId, int ackType, int result){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setAckType(ackType);
        ackMessage.setMsgId(msgId);
        return   new Gson().toJson(ackMessage);
    }
}
