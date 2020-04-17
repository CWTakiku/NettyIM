package com.takiku.im_lib;


import com.takiku.im_lib.protobuf.PackProtobuf;

import org.junit.Test;

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

import static com.takiku.im_lib.entity.AppMessage.MSG_REPLY_TYPE;
import static com.takiku.im_lib.entity.AppMessage.MSG_SENDED;
import static com.takiku.im_lib.entity.ShakeHandsMessage.AUTH_FAILED;
import static com.takiku.im_lib.entity.ShakeHandsMessage.AUTH_SUCCESS;

/**
 * <p>@ProjectName:     BoChat</p>
 * <p>@ClassName:       NettyServerDemo.java</p>
 * <p>@PackageName:     com.bochat.im.netty</p>
 * <b>
 * <p>@Description:     TCP netty服务端</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/02/15 14:42</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class NettyServerDemo {





    @Test
    public  void Server() {

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
        PackProtobuf.Pack replyPack=null;
        switch (pack.getPackType()){
            case SHAKEHANDS:
                PackProtobuf.ShakeHands shakeHands=pack.getShakeHands();
                String userId=shakeHands.getUserId();
                String token=shakeHands.getToken();
                String msgId=shakeHands.getMsgId();
                System.out.println("收到连接认证消息，该用户的id: "+userId+" 该用户的token: "+token );
                if (token.equals("your token")&&userId.equals("your userId")){ //连接认证成功

                    replyPack= PackProtobuf.Pack.newBuilder()
                            .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                            .setShakeHands(shakeHands.toBuilder().setStatusReport(AUTH_SUCCESS))
                            .build();
                    ChannelContainer.getInstance().saveChannel(new NettyChannel(userId, ctx.channel()));
                }else {
                    replyPack= PackProtobuf.Pack.newBuilder()
                            .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                            .setShakeHands(shakeHands.toBuilder().setStatusReport(AUTH_FAILED))
                            .build();
                    ctx.channel().writeAndFlush(replyPack);
                    ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());

                    return;
                }
                if (replyPack!=null){
                    ChannelContainer.getInstance().getActiveChannelByUserId(userId).getChannel().writeAndFlush(replyPack);
                }
                break;
            case HEART:
                PackProtobuf.Heart heart=pack.getHeart();
                System.out.println("收到客户端心跳消息,该用户id："+heart.getUserId());
                break;
            case MSG:
                PackProtobuf.Msg message=pack.getMsg();
                System.out.println("收到客户端发送过来的消息:"+message.toString());
                PackProtobuf.Reply reply=PackProtobuf.Reply.newBuilder().setReplyType(MSG_REPLY_TYPE)
                        .setMsgId(message.getHead().getMsgId())
                        .setStatusReport(MSG_SENDED) //已发送状态回执
                        .build();
                replyPack=PackProtobuf.Pack.newBuilder()
                        .setPackType(PackProtobuf.Pack.PackType.REPLY)
                        .setReply(reply).build();
                ChannelContainer.getInstance().getActiveChannelByUserId(message.getHead().getFromId()).getChannel().writeAndFlush(replyPack);

        }


//        System.out.println("收到来自客户端的消息：" + message);
//        int msgType = message.getHead().getMsgType();
//        switch (msgType) {
//            // 握手消息
//            case 1001: {
//                String fromId = message.getHead().getFromId();
//                JSONObject jsonObj = JSON.parseObject(message.getHead().getExtend());
//                String token = jsonObj.getString("token");
//                JSONObject resp = new JSONObject();
//                if (token.equals("token_" + fromId)) {
//                    resp.put("status", 1);
//                    // 握手成功后，保存用户通道
//                    ChannelContainer.getInstance().saveChannel(new NettyChannel(fromId, ctx.channel()));
//                } else {
//                    resp.put("status", -1);
//                    ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
//                }
//
//                message = message.toBuilder().setHead(message.getHead().toBuilder().setExtend(resp.toString()).build()).build();
//                ChannelContainer.getInstance().getActiveChannelByUserId(fromId).getChannel().writeAndFlush(message);
//                break;
//            }
//
//            // 心跳消息
//            case 1002: {
//                // 收到心跳消息，原样返回
//                String fromId = message.getHead().getFromId();
//                ChannelContainer.getInstance().getActiveChannelByUserId(fromId).getChannel().writeAndFlush(message);
//                break;
//            }
//
//            case 2001: {
//                // 收到2001或3001消息，返回给客户端消息发送状态报告
//                String fromId = message.getHead().getFromId();
//                MessageProtobuf.Msg.Builder sentReportMsgBuilder = MessageProtobuf.Msg.newBuilder();
//                MessageProtobuf.Head.Builder sentReportHeadBuilder = MessageProtobuf.Head.newBuilder();
//                sentReportHeadBuilder.setMsgId(message.getHead().getMsgId());
//                sentReportHeadBuilder.setMsgType(1010);
//                sentReportHeadBuilder.setTimestamp(System.currentTimeMillis());
//                sentReportHeadBuilder.setStatusReport(1);
//                sentReportMsgBuilder.setHead(sentReportHeadBuilder.build());
//                ChannelContainer.getInstance().getActiveChannelByUserId(fromId).getChannel().writeAndFlush(sentReportMsgBuilder.build());
//
//                // 同时转发消息到接收方
//                String toId = message.getHead().getToId();
//                ChannelContainer.getInstance().getActiveChannelByUserId(toId).getChannel().writeAndFlush(message);
//                break;
//            }
//
//            case 3001: {
//                // todo 群聊，自己实现吧，toId可以是群id，根据群id查找所有在线用户的id，循环遍历channel发送即可。
//                break;
//            }
//
//            default:
//                break;
    //    }
    }

    public static class ChannelContainer {

        private ChannelContainer() {

        }

        private static final ChannelContainer INSTANCE = new ChannelContainer();

        public static ChannelContainer getInstance() {
            return INSTANCE;
        }

        private final Map<String, NettyChannel> CHANNELS = new ConcurrentHashMap<>();

        public void saveChannel(NettyChannel channel) {
            if (channel == null) {
                return;
            }
            CHANNELS.put(channel.getChannelId(), channel);
        }

        public NettyChannel removeChannelIfConnectNoActive(Channel channel) {
            if (channel == null) {
                return null;
            }

            String channelId = channel.id().toString();

            return removeChannelIfConnectNoActive(channelId);
        }

        public NettyChannel removeChannelIfConnectNoActive(String channelId) {
            if (CHANNELS.containsKey(channelId) && !CHANNELS.get(channelId).isActive()) {
                return CHANNELS.remove(channelId);
            }

            return null;
        }

        public String getUserIdByChannel(Channel channel) {
            return getUserIdByChannel(channel.id().toString());
        }

        public String getUserIdByChannel(String channelId) {
            if (CHANNELS.containsKey(channelId)) {
                return CHANNELS.get(channelId).getUserId();
            }

            return null;
        }

        public NettyChannel getActiveChannelByUserId(String userId) {
            for (Map.Entry<String, NettyChannel> entry : CHANNELS.entrySet()) {
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
