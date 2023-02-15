package com.takiku.im_lib.internal.connection;

import android.util.Log;

import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.OnResponseListener;
import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.internal.handler.internalhandler.HeartbeatChannelHandler;
import com.takiku.im_lib.internal.handler.internalhandler.MessageChannelHandler;
import com.takiku.im_lib.internal.MessageParser;
import com.takiku.im_lib.internal.handler.internalhandler.StatusChannelHandler;
import com.takiku.im_lib.internal.handler.internalhandler.WebSocketClientHandler;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.im_lib.util.LRUMap;
import com.takiku.im_lib.util.LogUtil;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;


public class RealConnection  implements Connection {

    private static final AtomicLong NETID_GENERATOR = new AtomicLong(0);

    private Channel channel;
    private final ConnectionPool connectionPool;
    private Bootstrap bootstrap;
    private Object heartBeatMsg;
    private MessageParser messageParser;
    private boolean hasInit=false;
    private InetSocketAddress inetSocketAddress;
    private LinkedHashMap<String, ChannelHandler> handlers;
    private EventListener eventListener;
    private volatile boolean reConnect;
    private connectionBrokenListener connectionBrokenListener;
    private volatile LRUMap<String,Response> responseLRUMap;
    private Serializable netId;
    private @IMProtocol int protocol;
    private  WebSocketClientHandler webSocketClientHandler;


    public RealConnection(ConnectionPool connectionPool, InetSocketAddress inetSocketAddress, EventListener eventListener){
        this.inetSocketAddress=inetSocketAddress;
        this.connectionPool=connectionPool;
        this.eventListener=eventListener;
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // 设置禁用nagle算法
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        reConnect=true;
        responseLRUMap=new LRUMap<>(30);
        netId = NETID_GENERATOR.getAndIncrement();
    }
    public void release(boolean reConnect){
        this.reConnect=reConnect;
        if (channel!=null){
            if (protocol == IMProtocol.PRIVATE){
                removeHandler(HeartbeatChannelHandler.class.getSimpleName(),channel);
            }else if (protocol == IMProtocol.WEB_SOCKET){
                removeHandler(HttpClientCodec.class.getSimpleName(),channel);
                removeHandler(HttpObjectAggregator.class.getSimpleName(),channel);
                removeHandler(WebSocketClientHandler.class.getSimpleName(),channel);
            }
            removeHandler(StatusChannelHandler.class.getSimpleName(),channel);
            removeHandler(MessageChannelHandler.class.getSimpleName(),channel);

            if (handlers!=null){
                for (String key : handlers.keySet()) {
                    removeHandler(key, channel);
                }
            }
            if (channel!=null){
                channel.close();
                channel=null;
            }
        }
        if (bootstrap != null&&bootstrap.group()!=null) {
            bootstrap.group().shutdownGracefully();
        }
        connectionPool.destroyWorkLoopGroup();
        bootstrap=null;
        eventListener.connectionReleased(this);
    }
    /**
     * 移除指定handler
     *
     * @param handlerName
     */
    private void removeHandler(String handlerName, Channel channel) {
        try {
            if (channel.pipeline()!=null){
                if (channel.pipeline().get(handlerName) != null) {
                    channel.pipeline().remove(handlerName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ChannelInitializerHandler(final Codec codec, @IMProtocol int protocol, HashMap<String,Object> wsHeaderMap,
                                          final Object heartBeatMsg,
                                          final LinkedHashMap<String, ChannelHandler> handlers,
                                          final int heartbeatInterval,
                                          final MessageParser messageParser,
                                          final connectionBrokenListener connectionBrokenListener,
                                          final int maxFrameLength
                                         ) throws AuthException {
        if (hasInit){
            return;
        }
        this.handlers=handlers;
        this.heartBeatMsg=heartBeatMsg;
        this.protocol = protocol;
        this.connectionBrokenListener=connectionBrokenListener;
        this.messageParser=messageParser;
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel ) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                //解决tcp拆包、粘包
                if (protocol == IMProtocol.PRIVATE){
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameLength,
                            0, 2, 0, 2));
                    //编解码支持
                    if (codec==null){
                        throw new  IllegalArgumentException("codec is null");
                    }
                    messageParser.addShakeResultListener(new MessageParser.onShakeHandsResultListener() {
                        @Override
                        public void shakeHandsResult(boolean isSuccess) {
                            if (isSuccess){
                                if (heartBeatMsg!=null){ //握手成功且设置了心跳包，则里面启动心跳机制
                                    addHeartbeatHandler(connectionPool,heartbeatInterval);
                                }
                            }else {
                                release(false);
                            }
                        }
                    });

                    pipeline.addLast(codec.EnCoder().getClass().getSimpleName(),codec.EnCoder());
                    pipeline.addLast(codec.DeCoder().getClass().getSimpleName(),codec.DeCoder());

                    if (messageParser.getMessageShakeHandsHandler() == null){ //如果没有握手消息且设置了心跳包，则直接添加心跳机制，否则等握手成功后添加心跳机制
                        if (heartBeatMsg!=null){
//                        // 3次心跳没响应，代表连接已断开
                            addHeartbeatHandler(connectionPool,heartbeatInterval);

                        }
                    }
                    pipeline.addLast(StatusChannelHandler.class.getSimpleName(),new StatusChannelHandler(eventListener,connectionBrokenListener));
                    pipeline.addLast(MessageChannelHandler.class.getSimpleName(),new MessageChannelHandler(messageParser));
                }else if (protocol == IMProtocol.WEB_SOCKET){
                    URI uri   = new  URI(inetSocketAddress.getHostName());
                    // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
                    // If you change it to V00, ping is not supported and remember to change
                    // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
                    HttpHeaders customHeaders = null;
                    if (wsHeaderMap!=null&&!wsHeaderMap.isEmpty()){
                        customHeaders = new DefaultHttpHeaders();
                        for (String key:wsHeaderMap.keySet()){
                            customHeaders.add(key,wsHeaderMap.get(key));
                        }
                    }
                    SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

                    webSocketClientHandler =
                            new WebSocketClientHandler(
                                    WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false,customHeaders,maxFrameLength),
                                    messageParser,connectionBrokenListener,eventListener);
                    //支持wss
                    pipeline.addFirst(sslCtx.newHandler(channel.alloc(), uri.getHost(),uri.getPort()));
                    pipeline.addLast(HttpClientCodec.class.getSimpleName(), new HttpClientCodec());
                    pipeline.addLast(HttpObjectAggregator.class.getSimpleName(), new HttpObjectAggregator(65535));
                    pipeline.addLast(WebSocketClientHandler.class.getSimpleName(), webSocketClientHandler);
                    addHeartbeatHandler(pipeline,connectionPool,heartbeatInterval);
                }

                if (handlers!=null){
                    for (String key : handlers.keySet()) {
                        pipeline.addLast(key, handlers.get(key));
                    }
                }
                hasInit=true;
            }
        });
    }
    public void addHeartbeatHandler(ConnectionPool connectionPool,int heartbeatInterval) {
        if (channel == null || channel.pipeline() == null) {
            return;
        }
        try {
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳时间内没得到服务端响应，即可代表连接已断开
            channel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (channel.pipeline().get(HeartbeatChannelHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(HeartbeatChannelHandler.class.getSimpleName());
            }
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().addLast(HeartbeatChannelHandler.class.getSimpleName(),
                        new HeartbeatChannelHandler(connectionPool,heartBeatMsg,connectionBrokenListener));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addHeartbeatHandler(ChannelPipeline pipeline,ConnectionPool connectionPool,int heartbeatInterval) {
        if (pipeline  == null) {
            return;
        }
        try {
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (pipeline.get(IdleStateHandler.class.getSimpleName()) != null) {
                pipeline.remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳时间内没得到服务端响应，即可代表连接已断开
            pipeline.addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (pipeline.get(HeartbeatChannelHandler.class.getSimpleName()) != null) {
                pipeline.remove(HeartbeatChannelHandler.class.getSimpleName());
            }
            if (pipeline.get(IdleStateHandler.class.getSimpleName()) != null) {
                pipeline.addLast(HeartbeatChannelHandler.class.getSimpleName(),
                        new HeartbeatChannelHandler(connectionPool,heartBeatMsg,connectionBrokenListener));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public InetSocketAddress InetSocketAddress() {
        return inetSocketAddress;
    }

    @Override
    public Serializable generateNetId() {
        return netId;
    }



    public void connect(int connectTimeout) throws InterruptedException {

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
        try {
            URI uri = new URI(inetSocketAddress.getHostName());
            if (protocol == IMProtocol.WEB_SOCKET){
                //等待握手成功
                channel=  bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
                webSocketClientHandler.handshakeFuture().sync();
            }else {
                channel=  bootstrap.connect(ip, port).sync().channel();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }
    public boolean isHealth(){
      if (channel!=null&&channel.isActive()){
          return true;
      }else {
          return false;
      }
    }

    public TcpStream newStream(IMClient client,StreamAllocation streamAllocation){
        TcpStream tcpStream=new Stream(client,streamAllocation,channel);
        return tcpStream;
    }


    public synchronized LRUMap<String,Response> responseLRUMap(){
        return responseLRUMap;
    }

    /**
     * 注册消息消费者
     * @param request
     * @param consumers
     */
    public void registerConsumer(final Request request, List<Consumer> consumers){
        messageParser.registerConsumer(request.requestTag, consumers);
    }
    public void registerAckConsumer(final Request request,Consumer ackConsumer){
        if (ackConsumer!=null){
            messageParser.registerAckConsumer(request.requestTag,ackConsumer ,new OnResponseListener() {
                @Override
                public void onResponseArrive(Response response) {
                    response.request=request;
                    responseLRUMap.put(request.requestTag,response);
                }
            });
        }
    }

    public void unRegisterConsumer(final  Request request){
        messageParser.unRegisterConsumer(request.requestTag);
    }
    public boolean isRegister(final  Request request){
       return    messageParser.isRegisterConsumer(request.requestTag);
    }

    public boolean isReConnect(){
        return reConnect;
    }

   public   interface connectionBrokenListener{
        void connectionBroken();
    }



}
