package com.takiku.im_lib.client;

import androidx.annotation.Nullable;

import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.RealCall;
import com.takiku.im_lib.frameCodec.DefaultLengthFieldBasedFrameCodec;
import com.takiku.im_lib.frameCodec.FrameCodec;
import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.dispatcher.Dispatcher;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.internal.Internal;
import com.takiku.im_lib.internal.connection.ConnectionPool;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.internal.MessageParser;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.im_lib.util.IdWorker;
import com.takiku.im_lib.util.LogUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandler;

/**
 * IMClient IM客户端SDK
 */
public class IMClient {


    static {


        Internal.instance = new Internal() {

            @Override
            public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
                return pool.get(address, streamAllocation);
            }

            @Override
            public void deduplicate(ConnectionPool pool) {
                pool.deduplicate();
            }

            @Override
            public void put(ConnectionPool pool, RealConnection connection) {
                pool.put(connection);
            }
        };
    }

    Dispatcher dispatcher;//消息分发器
    List<Interceptor> interceptors;//拦截器
    private int resendCount;//消息发送失败，重复次数
    int connectTimeout;//连接超时
    int sendTimeout;//发送超时，该时间段没收到ACK即认定为超时，会触发超时重发（如果设置为超时重发）
    boolean connectionRetryEnabled;//能否连接重试
    boolean msgTriggerReconnectEnabled; //是否发送消息能触发重连（如果连接已经断的情况
    boolean readerIdleReconnectEnabled; //读空闲是否触发重连
    long connectRetryInterval;//连接重试间隔
    int connectRetryCount;//最大重连次数
    int heartIntervalForeground;//前台心跳间隔
    int heartIntervalBackground;//后台心跳间隔
    int readerIdleTimeForeground;//前台读空闲时间，读空闲超时会触发重连机制
    int readerIdleTimeBackground;//后台读空闲时间
    boolean isBackground;//是否处于后台
    EventListener.Factory eventListenerFactory;
    ConnectionPool connectionPool;
    Codec codec;//编解码器
    FrameCodec frameCodec;// 自定义tcp协议中的装包拆包
    LinkedHashMap<String, ChannelHandler> channelHandlerLinkedHashMap;//channelhandler，开发者可以添加自己的channelhandler
    Object heartBeatMsg;//心跳包
    List<Address> addressList;//连接地址列表，连接失败会自动切换
    MessageParser messageParser;//消息解析器
    Consumer ackConsumer;//消息ACK机制，开发者需要设置自己的ACKConsumer， Observable返回的是否是该请求的ACK包
    @IMProtocol
    int protocol = IMProtocol.PRIVATE;
    int port;//本地端口号
    HashMap<String, Object> wsHeaderMap;//WS握手的头
    int maxFrameLength;//最大帧长
    int lengthFieldLength; //TCP 装包拆包的长度字段的占用字节数



    public IMClient() {
        this(new Builder());
    }


    IMClient(Builder builder) {
        this.resendCount = builder.resendCount;
        this.dispatcher = builder.dispatcher;
        this.connectTimeout = builder.connectTimeout;
        this.heartIntervalBackground = builder.heartIntervalBackground;
        this.heartIntervalForeground = builder.heartIntervalForeground;
        this.connectionPool = builder.connectionPool;
        this.eventListenerFactory = builder.eventListenerFactory;
        this.connectionRetryEnabled = builder.connectionRetryEnabled;
        this.codec = builder.codec;
        this.channelHandlerLinkedHashMap = builder.channelHandlerLinkedHashMap;
        this.heartBeatMsg = builder.heartBeatMsg;
        this.sendTimeout = builder.sendTimeout;
        this.addressList = builder.addressList;
        this.isBackground = builder.isBackground;
        this.messageParser = builder.messageParser;
        this.ackConsumer = builder.ackConsumer;
        this.protocol = builder.protocol;
        this.wsHeaderMap = builder.wsHeaderMap;
        this.connectRetryCount = builder.connectRetryCount;
        this.connectRetryInterval = builder.connectRetryInterval;
        this.maxFrameLength = builder.maxFrameLength;
        this.msgTriggerReconnectEnabled = builder.msgTriggerReconnectEnabled;
        this.port = builder.port;
        this.readerIdleTimeForeground = builder.readerIdleTimeForeground;
        this.readerIdleTimeBackground = builder.readerIdleTimeBackground;
        this.readerIdleReconnectEnabled = builder.readerIdleReconnectEnabled;
        this.lengthFieldLength = builder.lengthFieldLength;
        this.frameCodec = builder.frameCodec;
    }

    public void startConnect() {
        checkAddressList(addressList);
        Call call = newCall(new ConnectRequest());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {

            }
        });
    }

    public void disConnect() {
        Internal.instance.deduplicate(connectionPool);
    }


    public Long getMsgSerialId() {
        if (connectionPool == null || connectionPool.realConnection() == null || !connectionPool.realConnection().isHealth()) {
            return 0L;
        }
        return IdWorker.nextId(connectionPool.realConnection().generateNetId());
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public int resendCount() {
        return resendCount;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public boolean isConnected(){
        if ( connectionPool()== null||connectionPool().realConnection()==null){
            return false;
        }
        return connectionPool().realConnection().isHealth();
    }

    public Codec codec() {
        return codec;
    }

    public int protocol() {
        return protocol;
    }

    public int port(){return port;}

    public int connectTimeout() {
        return connectTimeout;
    }

    public int maxFrameLength(){
        return maxFrameLength;
    }
    public boolean msgTriggerReconnectEnabled(){
        return msgTriggerReconnectEnabled;
    }

    public int sendTimeout() {
        return sendTimeout;
    }

    public int lengthFieldLength(){return lengthFieldLength;}


    public List<Address> addressList() {
        return addressList;
    }

    public MessageParser messageParser() {
        return messageParser;
    }

    public HashMap<String, Object> wsHeaderMap() {
        return wsHeaderMap;
    }

    public FrameCodec frameCodec(){return frameCodec;}


    /**
     * 发送消息
     *
     * @param request
     * @return
     */
    public Call newCall(Request request) {
        return new RealCall(this, request);
    }


    public Object heartBeatMsg() {
        return heartBeatMsg;
    }

    public Consumer ackConsumer() {
        return ackConsumer;
    }

    public int heartInterval() {
        if (isBackground) {
            return heartIntervalBackground;
        } else {
            return heartIntervalForeground;
        }
    }
    public int readerIdleTime(){
        if (isBackground){
            return readerIdleTimeBackground;
        }
        return readerIdleTimeForeground;
    }

    public LinkedHashMap<String, ChannelHandler> customChannelHandlerLinkedHashMap() {
        return channelHandlerLinkedHashMap;
    }


    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }

    public boolean connectionRetryEnabled() {
        return connectionRetryEnabled;
    }
    public boolean readerIdleReconnectEnabled(){return readerIdleReconnectEnabled;}

    public long connectRetryInterval(){
        return connectRetryInterval;
    }
    public int connectRetryCount(){
        return connectRetryCount;
    }


    /**
     * 设置前后台，将自动切换心跳间隔
     *
     * @param isBackground
     */
    public void setBackground(boolean isBackground) {
        this.isBackground = isBackground;
        connectionPool().changeHeartbeatInterval(heartInterval(),readerIdleTime(),readerIdleReconnectEnabled());
    }

    public static final class Builder {
        Dispatcher dispatcher;
        final List<Interceptor> interceptors = new ArrayList<>();
        int connectTimeout;
        int sendTimeout;
        int resendCount;
        boolean connectionRetryEnabled;
        int heartIntervalForeground;
        int heartIntervalBackground;
        int readerIdleTimeForeground;
        int readerIdleTimeBackground;
        long connectRetryInterval;
        int maxFrameLength;
        boolean msgTriggerReconnectEnabled;
        boolean readerIdleReconnectEnabled;
        boolean isBackground;
        int lengthFieldLength;
        FrameCodec frameCodec;
        EventListener.Factory eventListenerFactory;
        ConnectionPool connectionPool;
        Cache cache;
        Authenticator authenticator;

        List<Address> addressList;
        @Nullable
        Codec codec;
        LinkedHashMap<String, ChannelHandler> channelHandlerLinkedHashMap;
        Object heartBeatMsg;
        MessageParser messageParser;
        Consumer ackConsumer;
        HashMap<String, Object> wsHeaderMap;
        int connectRetryCount;
        @IMProtocol
        int protocol = IMProtocol.PRIVATE;
        @Deprecated
        int port;

        public Builder() {
            dispatcher = new Dispatcher();
            heartIntervalForeground = 30 * 1000;
            heartIntervalBackground = 50 * 1000;
            readerIdleTimeForeground = heartIntervalForeground * 3;
            readerIdleTimeBackground = heartIntervalBackground * 3;
            isBackground = true;
            resendCount = 3;
            sendTimeout = 5 * 1000;
            connectTimeout = 10 * 1000;
            connectRetryInterval = 500;
            maxFrameLength = 65535;
            connectRetryCount = 3;
            lengthFieldLength = 2;
            connectionRetryEnabled = true;
            msgTriggerReconnectEnabled = true;
            readerIdleReconnectEnabled = true;
            wsHeaderMap = new HashMap<>();
            addressList = new ArrayList<>();
            this.connectionPool = new ConnectionPool();
            eventListenerFactory = EventListener.factory(EventListener.NONE);
            messageParser = new MessageParser();
            frameCodec = new DefaultLengthFieldBasedFrameCodec(lengthFieldLength,maxFrameLength);
        }

        public Builder setCodec(@Nullable Codec codec) {
            this.codec = codec;
            return this;
        }

        public Builder setFrameCodec(FrameCodec frameCodec){
            this.frameCodec = frameCodec;
            return this;
        }

        public Builder setProtocol(@IMProtocol int protocol) {
            this.protocol = protocol;
            return this;
        }
        @Deprecated
        public Builder setPort(int port){
            if (port < 0 || port > 0xFFFF)
                throw new IllegalArgumentException("port out of range:" + port);
            this.port = port;
            return this;
        }
        public Builder setOpenLog(boolean isOpen){
            LogUtil.setOpen(isOpen);
            return this;
        }
        public Builder setReaderIdleTimeForeground(long interval, TimeUnit unit){
            this.readerIdleTimeForeground = checkDuration("interval", interval, unit);
            return this;
        }

        public Builder setReaderIdleTimeBackground(long interval, TimeUnit unit){
            this.readerIdleTimeBackground = checkDuration("interval", interval, unit);
            return this;
        }

        /**
         * 请改用setFrameCodec(FrameCodec frameCodec)方法，设置具体的装包拆包器
         * @param lengthFieldLength
         * @return
         */
        @Deprecated
        public Builder setTCPLengthFieldLength(int lengthFieldLength){
            this.lengthFieldLength = lengthFieldLength;
            return this;
        }


        public Builder setConnectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout = checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder setAddressList(List<Address> addressList) {
            this.addressList = addressList;
            return this;
        }
        public Builder setMaxFrameLength(int maxFrameLength){
            this.maxFrameLength = maxFrameLength;
            return this;
        }


        public Builder addWsHeader(String key, String value) {
            this.wsHeaderMap.put(key, value);
            return this;
        }

        /**
         * 连接重试间隔时间
         * @param interval
         * @return
         */
        public Builder setConnectRetryInterval(int interval,TimeUnit unit) {
            this.connectRetryInterval = checkDuration("interval", interval, unit);
            return this;
        }

        public Builder addAddress(Address address) {
            if (address.type() == Address.Type.WS||address.type() == Address.Type.WSS) {
                URI uri = null;
                try {
                    uri = new URI(address.getUrl());
                    String protocol = uri.getScheme();
                    LogUtil.i("IMCient", "protocol " + protocol);
                    if (!("ws".equals(protocol)||("wss".equals(protocol)))) {
                        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            if (!addressList.contains(address)) {
                addressList.add(address);
            }
            return this;
        }

        public Builder setConnectRetryCount(int connectRetryCount){
            this.connectRetryCount = connectRetryCount;
            return this;
        }

        public Builder eventListener(EventListener eventListener) {
            if (eventListener == null) throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        public Builder setSendTimeout(long timeout, TimeUnit unit) {
            this.sendTimeout = checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder setResendCount(int resendCount) {
            this.resendCount = resendCount;
            return this;
        }

        public Builder setHeartIntervalForeground(int interval, TimeUnit unit) {
            this.heartIntervalForeground = checkDuration("interval", interval, unit);
            return this;
        }

        public Builder setHeartIntervalBackground(long interval, TimeUnit unit) {
            this.heartIntervalBackground = checkDuration("interval", interval, unit);
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public Builder addChannelHandler(String name, ChannelHandler channelHandler) {
            channelHandlerLinkedHashMap.put(name, channelHandler);
            return this;
        }

        public Builder setConnectionRetryEnabled(boolean connectionRetryEnabled) {
            this.connectionRetryEnabled = connectionRetryEnabled;
            return this;
        }
        public Builder setMsgTriggerReconnectEnabled(boolean msgTriggerReconnectEnabled) {
            this.msgTriggerReconnectEnabled = msgTriggerReconnectEnabled;
            return this;
        }
        public Builder setReaderIdleReconnectEnabled(boolean readerIdleReconnectEnabled){
            this.readerIdleReconnectEnabled = readerIdleReconnectEnabled;
            return this;
        }

        /**
         * 注册消息处理器
         *
         * @param messageHandler
         * @return
         */
        public Builder registerMessageHandler(MessageHandler messageHandler) {
            messageParser.registerMessageHandler(messageHandler);
            return this;
        }

        public Builder setBackground(boolean isBackground) {
            this.isBackground = isBackground;
            return this;
        }

        /**
         * 设置事件监听
         *
         * @param eventListener
         * @return
         */
        public Builder setEventListener(EventListener eventListener) {
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        public Builder setShakeHands(MessageShakeHandsHandler shakeHandler) {
            this.messageParser.registerMessageShakeHandsHandler(shakeHandler);
            return this;
        }

        public Builder setHeartBeatMsg(Object heartBeatMsg) {
            this.heartBeatMsg = heartBeatMsg;
            return this;
        }

        /**
         * 设置ACK机制,如果设置了，在request里有needACK,则必须收到ACK包 不然会回调onFailure
         *
         * @param ackConsumer
         * @return
         */
        public Builder setAckConsumer(Consumer ackConsumer) {
            this.ackConsumer = ackConsumer;
            return this;
        }


        public IMClient build() {
            return new IMClient(this);
        }
    }

    private static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    private static void checkAddressList(List<Address> addressList) {
        if (addressList == null || addressList.size() < 1) {
            throw new IllegalStateException("address == null");
        }
    }
}
