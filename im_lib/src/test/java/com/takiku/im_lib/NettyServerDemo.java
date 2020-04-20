package com.takiku.im_lib;


import com.takiku.im_lib.protobuf.PackProtobuf;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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

import static com.takiku.im_lib.NettyServerDemo.HEART_REPLY_TYPE;
import static com.takiku.im_lib.NettyServerDemo.MSG_REPLY_TYPE;
import static com.takiku.im_lib.NettyServerDemo.SHAKE_HANDS_REPLY_TYPE;
import static com.takiku.im_lib.NettyServerDemo.SHAKE_HANDS_STATUS_FAILED;
import static com.takiku.im_lib.NettyServerDemo.SHAKE_HANDS_STATUS_SUCCESS;
import static com.takiku.im_lib.NettyServerDemo.userMap;

/**
 * IMCient 服务端demo
 */
public class NettyServerDemo {

    public static final int MSG_REPLY_TYPE=0x10;
    public static final int HEART_REPLY_TYPE=0x11;
    public static final int SHAKE_HANDS_REPLY_TYPE=0x12;

    public static final int SHAKE_HANDS_STATUS_SUCCESS=1;
    public static final int SHAKE_HANDS_STATUS_FAILED=0;
    public static final int MSG_STATUS_SEND=1;
    public static final int MSG_STATUS_READ=2;

    public static Map<String,String>  userMap=new HashMap<>();

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
            ChannelFuture future = bootstrap.bind(8765).sync();
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
        userMap.put("user id1","token1");
        userMap.put("user id2","token2");
    }
}

class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = ServerHandler.class.getSimpleName();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("ServerHandler channelActive()" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("ServerHandler channelInactive()");
        // 用户断开连接后，移除channel
        ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
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
        switch (pack.getPackType()){
            case SHAKEHANDS:
                PackProtobuf.ShakeHands shakeHands=pack.getShakeHands();
                String userId=shakeHands.getUserId();
                String token=shakeHands.getToken();
                String msgId=shakeHands.getMsgId();
                System.out.println("收到连接认证消息，该用户的id: "+userId+" 该用户的token: "+token );

                if (userMap.containsKey(userId)&&token.equals(userMap.get(userId))){ //连接认证成功
                    ctx.channel().writeAndFlush(createShakeHandsResp(msgId,userId,SHAKE_HANDS_STATUS_SUCCESS));
                    ChannelContainer.getInstance().saveChannel(new NettyChannel(userId, ctx.channel()));
                }else {
                    ctx.channel().writeAndFlush(createShakeHandsResp(msgId,userId,SHAKE_HANDS_STATUS_FAILED));
                    ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
                    return;
                }
                break;
            case HEART:
                PackProtobuf.Heart heart=pack.getHeart();
                System.out.println("收到客户端心跳消息,该用户id："+heart.getUserId());
                ChannelContainer.getInstance().getActiveChannelByUserId(heart.getUserId()).getChannel().writeAndFlush(createHeartResp(heart.getUserId()));
                break;
            case MSG:
                PackProtobuf.Msg message=pack.getMsg();
                System.out.println("收到发送方客户端发送过来的消息:"+message.toString());
                ChannelContainer.getInstance().getActiveChannelByUserId(message.getHead().getFromId()).getChannel() //回给发送端消息已经发送
                        .writeAndFlush(createMsgReply(message.getHead().getFromId(),message.getHead().getMsgId(),MSG_REPLY_TYPE, NettyServerDemo.MSG_STATUS_SEND));
                if (ChannelContainer.getInstance().isOnline(message.getHead().getToId())){
                    ChannelContainer.getInstance().getActiveChannelByUserId(message.getHead().getToId()).getChannel() //转发给接受端
                            .writeAndFlush(pack);
                }else { //TODO 对方离线，缓存起来

                }


                break;
            case REPLY:
                PackProtobuf.Reply receiveReply=pack.getReply();
                System.out.println("收到接受方客户端响应的状态:"+receiveReply.toString());
                switch (receiveReply.getReplyType()){
                    case MSG_REPLY_TYPE://消息状态回复，转发给发送方是被送达了，还是被阅读了等
                        System.out.println("转发消息状态给发送方"+receiveReply.getUserId());
                        if (ChannelContainer.getInstance().isOnline(receiveReply.getUserId())) {
                            ChannelContainer.getInstance().getActiveChannelByUserId(receiveReply.getUserId()).getChannel().writeAndFlush(pack);
                        }else {//TODO 对方离线，缓存起来

                        }
                        break;

                }

                break;

        }
    }

    private PackProtobuf.Pack createHeartResp(String userId){
        PackProtobuf.Pack pack=    PackProtobuf.Pack.newBuilder().setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply(PackProtobuf.Reply.newBuilder().setReplyType(HEART_REPLY_TYPE).setUserId(userId).build())
                .build();
                return pack;
    }
    private PackProtobuf.Pack createShakeHandsResp(String msgId,String userId,int status){
       return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply(PackProtobuf.Reply.newBuilder()
                        .setReplyType(SHAKE_HANDS_REPLY_TYPE).setMsgId(msgId).setUserId(userId).setStatusReport(status).build())
                .build();
    }
    private PackProtobuf.Pack createMsgReply(String userId,String msgId,int replyType,int status){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply( PackProtobuf.Reply.newBuilder().setUserId(userId).setReplyType(replyType).setMsgId(msgId).setStatusReport(status).build())
                .build();
    }


    /**
     * 模拟其他用户发消息
     */
    private void mockOtherClientSendMsg() {
        PackProtobuf.Msg mockOtherClientMsg=PackProtobuf.Msg.newBuilder()
                .setHead(PackProtobuf.Head.newBuilder().setFromId("other userId").setToId("your userId").build())
                .setBody("other给你发送消息了")
                .build();
        PackProtobuf.Pack otherMsgPack=PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.MSG)
                .setMsg(mockOtherClientMsg)
                .build();

        ChannelContainer.getInstance().getActiveChannelByUserId(mockOtherClientMsg.getHead().getToId()).getChannel().writeAndFlush(otherMsgPack);
    }


    public static class ChannelContainer {

        private ChannelContainer() {

        }

        private static final ChannelContainer INSTANCE = new ChannelContainer();

        public static ChannelContainer getInstance() {
            return INSTANCE;
        }

        private final Map<String, NettyChannel> channelMap = new ConcurrentHashMap<>();

        public void saveChannel(NettyChannel channel) {
            if (channel == null) {
                return;
            }
            channelMap.put(channel.getChannelId(), channel);
        }

        public NettyChannel removeChannelIfConnectNoActive(Channel channel) {
            if (channel == null) {
                return null;
            }

            String channelId = channel.id().toString();

            return removeChannelIfConnectNoActive(channelId);
        }

        public NettyChannel removeChannelIfConnectNoActive(String channelId) {
            if (channelMap.containsKey(channelId) && !channelMap.get(channelId).isActive()) {
                return channelMap.remove(channelId);
            }

            return null;
        }

        public String getUserIdByChannel(Channel channel) {
            return getUserIdByChannel(channel.id().toString());
        }

        public String getUserIdByChannel(String channelId) {
            if (channelMap.containsKey(channelId)) {
                return channelMap.get(channelId).getUserId();
            }

            return null;
        }
        public boolean isOnline(String userId){
            if (getActiveChannelByUserId(userId)!=null){
                return true;
            }else {
                return false;
            }
        }

        public NettyChannel getActiveChannelByUserId(String userId) {
            for (Map.Entry<String, NettyChannel> entry : channelMap.entrySet()) {
                if (entry.getValue().getUserId().equals(userId) && entry.getValue().isActive()) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    public class NettyChannel {

        private String userId;
        private Channel channel;

        public NettyChannel(String userId, Channel channel) {
            this.userId = userId;
            this.channel = channel;
        }

        public String getChannelId() {
            return channel.id().toString();
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public boolean isActive() {
            return channel.isActive();
        }
    }
}
