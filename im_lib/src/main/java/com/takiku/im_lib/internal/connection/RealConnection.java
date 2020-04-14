package com.takiku.im_lib.internal.connection;

import android.os.Looper;

import com.google.protobuf.Internal;
import com.takiku.im_lib.Codec.Codec;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.dispatcher.Handshake;
import com.takiku.im_lib.internal.handler.InternalChannelHandler;
import com.takiku.im_lib.internal.handler.HeartbeatRespHandler;
import com.takiku.im_lib.internal.handler.LoginAuthHandler;
import com.takiku.im_lib.internal.handler.MessageHandler;
import com.takiku.im_lib.util.LRUMap;

import java.util.LinkedHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


public class RealConnection  implements Connection {

    private final Route route;
    private Channel channel;
    private final ConnectionPool connectionPool;
    private Bootstrap bootstrap;
    private TcpStream tcpStream;
    private LRUMap<Integer,com.google.protobuf.Internal.EnumLite> lruMap;
    int resultIndex=0;

    public RealConnection(ConnectionPool connectionPool, Route route){
        this.route=route;
        this.connectionPool=connectionPool;
        lruMap=new LRUMap(10);
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // 设置禁用nagle算法
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }
    public void ChannelInitializerHandler(final Codec codec, final com.google.protobuf.Internal.EnumLite loginAuth,final com.google.protobuf.Internal.EnumLite commonReply,
                                          final InternalChannelHandler loginAuthInternalChannelHandler, final InternalChannelHandler heartInternalChannelHandler,
                                          final InternalChannelHandler messageInternalChannelHandler,
                                          final LinkedHashMap<String, ChannelHandler> handlers){
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel ) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                //解决tcp拆包、粘包
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535,
                        0, 2, 0, 2));
                //编解码支持
                pipeline.addLast(codec.EnCoder().getClass().getSimpleName(),codec.EnCoder());
                pipeline.addLast(codec.DeCoder().getClass().getSimpleName(),codec.DeCoder());

                pipeline.addLast(LoginAuthHandler.class.getSimpleName(),new LoginAuthHandler(loginAuth, loginAuthInternalChannelHandler));
                pipeline.addLast(HeartbeatRespHandler.class.getSimpleName(),new HeartbeatRespHandler(heartInternalChannelHandler));
                if (handlers!=null){
                    for (String key : handlers.keySet()) {
                        pipeline.addLast(key, handlers.get(key));
                    }
                }
                pipeline.addLast(MessageHandler.class.getSimpleName(),new MessageHandler(messageInternalChannelHandler, commonReply, new MessageHandler.onResponseListener() {
                    @Override
                    public void onResponse(Object msg) {
                      lruMap.put(resultIndex++, (Internal.EnumLite) msg);
                    }
                }));
            }
        });
    }
    @Override
    public Route route() {
        return route;
    }

    @Override
    public Channel channel() {
        return channel;
    }


    public void connect( int connectTimeout) throws InterruptedException {
        // 设置连接超时时长
          if (Looper.getMainLooper().getThread()==Thread.currentThread()){
         String  s="1";
          }
          bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
          String ip=route.inetSocketAddress.getAddress().getHostAddress();
          int port=route.inetSocketAddress.getPort();
        String threadName=Thread.currentThread().getName();
        ChannelFuture channelFuture=  bootstrap.connect(ip,port);

        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (!future.isSuccess()){

                }
            }
        });
        channel=channelFuture.channel();
    }
    public TcpStream newStream(IMClient client,StreamAllocation streamAllocation){
        TcpStream tcpStream=new Stream(client,streamAllocation,channel);
        return tcpStream;
    }

    @Override
    public Handshake handshake() {
        return null;
    }

    public  LRUMap<Integer,com.google.protobuf.Internal.EnumLite> lruMap(){
        return lruMap;
    }
}
