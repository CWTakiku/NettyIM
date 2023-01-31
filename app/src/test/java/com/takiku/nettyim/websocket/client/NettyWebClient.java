package com.takiku.nettyim.websocket.client;

import org.junit.Test;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * @author chengwl
 * @des
 * @date:2022/11/11
 */
public class NettyWebClient {
    private Channel channel;

    @Test
    public void main() {
        runClient("ws://172.18.3.38:8848/ws");

    }

    private void runClient(String strUri) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            URI uri = new URI(strUri);
            String protocol = uri.getScheme();
            System.out.println(protocol);
            if (!"ws".equals(protocol)) {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }
            HttpHeaders customHeaders = new DefaultHttpHeaders();
            customHeaders.add("MyHeader", "MyValue");
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            System.out.println("start");
            final MyWebSocketClientHandler handler =
                    new MyWebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, customHeaders));
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("http-codec", new HttpClientCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                    pipeline.addLast("ws-handler", handler);
                }
            });
            MyWebSocketClientHandler.Print.info("===============Message客户端启动===============");
            channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();
            channel.closeFuture().sync();
        } catch (Exception e) {
            MyWebSocketClientHandler.Print.info(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }
}
