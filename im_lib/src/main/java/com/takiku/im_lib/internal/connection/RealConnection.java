package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.dispatcher.Handshake;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.handler.HeartbeatChannelHandler;
import com.takiku.im_lib.internal.handler.InternalChannelHandler;
import com.takiku.im_lib.internal.handler.HeartbeatRespChannelHandler;
import com.takiku.im_lib.internal.handler.LoginAuthChannelHandler;
import com.takiku.im_lib.internal.handler.MessageRespHandler;
import com.takiku.im_lib.internal.handler.MessageRespChannelHandler;
import com.takiku.im_lib.internal.handler.ShakeHandsHandler;
import com.takiku.im_lib.internal.handler.StatusChannelHandler;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.util.LRUMap;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;


public class RealConnection  implements Connection {


    private Channel channel;
    private final ConnectionPool connectionPool;
    private Bootstrap bootstrap;
    private TcpStream tcpStream;
    private static volatile LRUMap<String,Object> lruMap;
    private com.google.protobuf.GeneratedMessageV3 heartBeatMsg;
    private int heartbeatInterval;
    private boolean hasInit=false;
    private InetSocketAddress inetSocketAddress;
    private LinkedHashMap<String, ChannelHandler> handlers;
    private EventListener eventListener;

    public RealConnection(ConnectionPool connectionPool, InetSocketAddress inetSocketAddress, EventListener eventListener){
        this.inetSocketAddress=inetSocketAddress;
        this.connectionPool=connectionPool;
        this.eventListener=eventListener;
        lruMap=new LRUMap(10);
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // 设置禁用nagle算法
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }
    public void release(){

        removeHandler(LoginAuthChannelHandler.class.getSimpleName(),channel);
        removeHandler(HeartbeatChannelHandler.class.getSimpleName(),channel);
        removeHandler(MessageRespChannelHandler.class.getSimpleName(),channel);
        if (handlers!=null){
            for (String key : handlers.keySet()) {
                removeHandler(key, channel);
            }
        }

        channel.close();
        channel.eventLoop().shutdownGracefully();
        channel.close();
        channel.eventLoop().shutdownGracefully();
        channel=null;
    }
    /**
     * 移除指定handler
     *
     * @param handlerName
     */
    private void removeHandler(String handlerName, Channel channel) {
        try {
            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("移除handler失败，handlerName=" + handlerName);
        }
    }

    public void ChannelInitializerHandler(final Codec codec, final com.google.protobuf.GeneratedMessageV3 shakeHandsMsg,final com.google.protobuf.GeneratedMessageV3 heartBeatMsg,
                                          final ShakeHandsHandler shakeHandsHandler, final InternalChannelHandler heartInternalChannelHandler,
                                          final MessageRespHandler messageRespHandler,
                                          final LinkedHashMap<String, ChannelHandler> handlers,
                                          final connectionBrokenListener connectionBrokenListener) throws AuthException {
        if (hasInit){
            return;
        }
        this.heartBeatMsg=heartBeatMsg;
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

                pipeline.addLast(StatusChannelHandler.class.getSimpleName(),new StatusChannelHandler(eventListener,connectionBrokenListener));

                pipeline.addLast(LoginAuthChannelHandler.class.getSimpleName(),new LoginAuthChannelHandler(shakeHandsMsg, shakeHandsHandler, new LoginAuthChannelHandler.ShakeHandsListener() {
                    @Override
                    public void shakeHandsSuccess(boolean isSuccess) {
                        if (isSuccess){
                            if (heartBeatMsg!=null){ //握手成功且设置了心跳包，则里面启动心跳机制
                                addHeartbeatHandler(connectionPool,heartBeatMsg);
                            }
                        }else {
                            release();
                        }
                    }
                }));
                pipeline.addLast(HeartbeatRespChannelHandler.class.getSimpleName(),new HeartbeatRespChannelHandler(heartInternalChannelHandler));
                pipeline.addLast(MessageRespChannelHandler.class.getSimpleName(),new MessageRespChannelHandler(messageRespHandler,new MessageRespChannelHandler.onResponseListener() {
                    @Override
                    public void onResponse(String tag,Object msg) {
                      lruMap.put(tag,  msg);
                    }
                }));
                if (handlers!=null){
                    for (String key : handlers.keySet()) {
                        pipeline.addLast(key, handlers.get(key));
                    }
                }
                if (shakeHandsMsg==null||shakeHandsHandler==null){ //如果没有握手消息且设置了心跳包，则直接添加心跳机制，否则等握手成功后添加心跳机制
                    if (heartBeatMsg!=null){
                        addHeartbeatHandler(connectionPool,heartBeatMsg);
                    }
                }
                hasInit=true;
            }
        });
    }
    public void addHeartbeatHandler(ConnectionPool connectionPool,com.google.protobuf.GeneratedMessageV3 heartBeatMsg) {
        if (channel == null || !channel.isActive() || channel.pipeline() == null) {
            return;
        }

        try {
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳没响应，代表连接已断开
            channel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (channel.pipeline().get(HeartbeatChannelHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(HeartbeatChannelHandler.class.getSimpleName());
            }
            if (channel.pipeline().get(MessageRespChannelHandler.class.getSimpleName()) != null) {
                channel.pipeline().addBefore(MessageRespChannelHandler.class.getSimpleName(), HeartbeatChannelHandler.class.getSimpleName(),
                        new HeartbeatChannelHandler(connectionPool,heartBeatMsg));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("添加心跳消息管理handler失败，reason：" + e.getMessage());
        }
    }


    @Override
    public Channel channel() {
        return channel;
    }


    public void connect( int connectTimeout) throws InterruptedException {
          eventListener.connectStart(inetSocketAddress);
        // 设置连接超时时长
          bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
           String ip;
          if (inetSocketAddress.getAddress()!=null){
              ip= inetSocketAddress.getAddress().getHostAddress();
          }else {
              ip=inetSocketAddress.getHostName();
          }
          int port=inetSocketAddress.getPort();
          String threadName=Thread.currentThread().getName();
         channel=  bootstrap.connect(ip,port).sync().channel();

//        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
//            @Override
//            public void operationComplete(Future<? super Void> future) throws Exception {
//                if (!future.isSuccess()){
//
//                }
//            }
//        });
//        channel=channelFuture.channel();
    }
    public boolean isHealth(){
      if (channel!=null&&channel.isActive()){
          return true;
      }else {
          return false;
      }
    }

    public TcpStream newStream(IMClient client,StreamAllocation streamAllocation,int heartbeatInterval){
        this.heartbeatInterval=heartbeatInterval;
        TcpStream tcpStream=new Stream(client,streamAllocation,channel);
        return tcpStream;
    }

    @Override
    public Handshake handshake() {
        return null;
    }

    public synchronized static    LRUMap<String,Object> lruMap(){
        return lruMap;
    }

   public   interface connectionBrokenListener{
        void connectionBroken();
    }

}
