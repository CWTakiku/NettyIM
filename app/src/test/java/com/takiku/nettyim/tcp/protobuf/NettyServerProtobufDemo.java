package com.takiku.nettyim.tcp.protobuf;


import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.udp.NettyUdpServer;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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





/**
 * IMCient 服务端demo
 */
public class NettyServerProtobufDemo {

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
                    pipeline.addLast(new ServerProtobufHandler());
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
            NettyUdpServer.Print.info("==============TCP服务端启动===============");

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


