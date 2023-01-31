package com.takiku.im_lib.server;



import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class NettyWebServer {
    @Test
    public void run() {
        runServer(8804);
    }

    private void runServer(int port) {
     MyWebSocketServerHandler.Print.info("===============Message服务端启动===============");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("codec-http", new HttpServerCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));//聚合器
                    pipeline.addLast("handler", new MyWebSocketServerHandler());
                }
            });
            Channel ch = b.bind(port).sync().channel();
          MyWebSocketServerHandler.Print.info("Message服务器启动成功：" + ch.toString());
            ch.closeFuture().sync();
        } catch (Exception e) {
           MyWebSocketServerHandler.Print.info("Message服务运行异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
          MyWebSocketServerHandler.Print.info("Message服务已关闭");
        }
    }
}