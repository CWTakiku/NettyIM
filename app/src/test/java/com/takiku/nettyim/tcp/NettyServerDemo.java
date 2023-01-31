package com.takiku.nettyim.tcp;


import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.Session;
import com.takiku.nettyim.SessionManager;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;


import static com.takiku.nettyim.tcp.NettyServerDemo.MSG_ACK_TYPE;
import static com.takiku.nettyim.tcp.NettyServerDemo.MSG_REPLY_TYPE;
import static com.takiku.nettyim.tcp.NettyServerDemo.SHAKE_HANDS_ACK_TYPE;
import static com.takiku.nettyim.tcp.NettyServerDemo.SHAKE_HANDS_STATUS_FAILED;
import static com.takiku.nettyim.tcp.NettyServerDemo.SHAKE_HANDS_STATUS_SUCCESS;
import static com.takiku.nettyim.tcp.NettyServerDemo.offLine;
import static com.takiku.nettyim.tcp.NettyServerDemo.userMap;


/**
 * IMCient 服务端demo
 */
public class NettyServerDemo {

    public static final int MSG_REPLY_TYPE=0x10;
        public static final int HEART_ACK_TYPE=0x11;
    public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int MSG_ACK_TYPE=0x13;

    public static final int SHAKE_HANDS_STATUS_SUCCESS=1;
    public static final int SHAKE_HANDS_STATUS_FAILED=0;
    public static final int MSG_STATUS_SEND=1;//发送成功
    public static final int MSG_STATUS_READ=2;

    public static Map<String,String>  userMap=new HashMap<>();
    public static Map<String, List<PackProtobuf.Pack>> offLine=new HashMap<>();


    @Test
    public  void Server() {

        initUserDb();

        //boss线程监听端口，worker线程负责数据读写
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            //辅助启动类
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置线程池
            bootstrap.group(boss, worker);

            //设置socket工厂
            bootstrap.channel(NioServerSocketChannel.class);

            //设置管道工厂
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //获取管道
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535,
                            0, 2, 0, 2));
                    pipeline.addLast(new ProtobufDecoder(PackProtobuf.Pack.getDefaultInstance()));
                    pipeline.addLast(new ProtobufEncoder());
                    //处理类
                    pipeline.addLast(new ServerHandler());
                }
            });

            //设置TCP参数
            //1.链接缓冲池的大小（ServerSocketChannel的设置）
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            //维持链接的活跃，清除死链接(SocketChannel的设置)
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            //关闭延迟发送
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

            //绑定端口
            ChannelFuture future = bootstrap.bind(9081).sync();
            System.out.println("server start ...... ");

            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出，释放线程池资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private void initUserDb() {
        userMap.put("userid1","token1");
        userMap.put("userid2","token2");
    }
}
@ChannelHandler.Sharable
class ServerHandler extends ChannelInboundHandlerAdapter {

    private final SessionManager sessionManager = SessionManager.getInstance();

    private static final String TAG = ServerHandler.class.getSimpleName();

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
                    session.writeAndFlush(createAck(heart.getUserId(), NettyServerDemo.HEART_ACK_TYPE,0));
                }
                break;
            case MSG:
                PackProtobuf.Msg message=pack.getMsg();
                System.out.println("收到发送方客户端发送过来的消息:"+message.toString());
                if (session!=null){
                    System.out.println("服务端发送ACK给发送端");
                    session.writeAndFlush(createAck(message.getHead().getMsgId(),MSG_ACK_TYPE, NettyServerDemo.MSG_STATUS_SEND));//服务端发送Ack给发送端
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
