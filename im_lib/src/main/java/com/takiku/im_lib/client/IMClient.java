package com.takiku.im_lib.client;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.RealCall;
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
import com.takiku.im_lib.util.IdWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandler;

/**
 * IMClient IM客户端SDK
 */
public class IMClient {



    static {
        Internal.instance = new Internal(){

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
     int heartIntervalForeground;//前台心跳间隔
     int heartIntervalBackground;//后台心跳间隔
     boolean isBackground;//是否处于后台
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     Codec codec;//编解码器
     LinkedHashMap<String , ChannelHandler> channelHandlerLinkedHashMap;//channelhandler，开发者可以添加自己的channelhandler
     com.google.protobuf.GeneratedMessageV3 heartBeatMsg;//心跳包
     List<Address> addressList;//连接地址列表，连接失败会自动切换
     MessageParser messageParser;//消息解析器
     Consumer ackConsumer;//消息ACK机制，开发者需要设置自己的ACKConsumer， Observable返回的是否是该请求的ACK包


    public IMClient(){this(new Builder());}


    IMClient(Builder builder){
       this.resendCount=builder.resendCount;
       this.dispatcher=builder.dispatcher;
       this.connectTimeout=builder.connectTimeout;
       this.heartIntervalBackground=builder.heartIntervalBackground;
       this.heartIntervalForeground=builder.heartIntervalForeground;
       this.connectionPool=builder.connectionPool;
       this.eventListenerFactory=builder.eventListenerFactory;
       this.connectionRetryEnabled=builder.connectionRetryEnabled;
       this.codec=builder.codec;
       this.channelHandlerLinkedHashMap=builder.channelHandlerLinkedHashMap;
       this.heartBeatMsg=builder.heartBeatMsg;
       this.sendTimeout=builder.sendTimeout;
       this.addressList=builder.addressList;
       this.isBackground=builder.isBackground;
       this.messageParser=builder.messageParser;
       this.ackConsumer=builder.ackConsumer;
    }

    public void startConnect() {
        checkAddressList(addressList);
        Call call= newCall( new ConnectRequest());
        call.enqueue(new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {

          }

          @Override
          public void onResponse(Call call, Response response)  {

          }
      });
    }
    public void disConnect(){
       Internal.instance.deduplicate(connectionPool);
    }


    public Long getMsgSerialId(){
        if (connectionPool == null||connectionPool.realConnection() == null||!connectionPool.realConnection().isHealth()){
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

    public int resendCount(){return resendCount;}

    public ConnectionPool connectionPool(){return  connectionPool;};


    public Codec codec(){return codec;}

    public int connectTimeout(){
        return connectTimeout;
    }

    public int sendTimeout(){return sendTimeout;}

    public List<Address> addressList(){ return addressList; }

    public MessageParser messageParser(){return messageParser;}


    /**
     * 发送消息
     * @param request
     * @return
     */
     public Call newCall(Request request) {
        return new RealCall(this, request);
    }




    public com.google.protobuf.GeneratedMessageV3 heartBeatMsg(){ return heartBeatMsg; }

    public Consumer ackConsumer(){return ackConsumer;}

    public int heartInterval(){
        if (isBackground){
        return heartIntervalBackground;
        } else {
            return heartIntervalForeground;
        }
    }

    public  LinkedHashMap<String , ChannelHandler> customChannelHandlerLinkedHashMap(){ return channelHandlerLinkedHashMap; }



    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }

    public boolean connectionRetryEnabled(){return connectionRetryEnabled;}

    /**
     * 设置前后台，将自动切换心跳间隔
     * @param isBackground
     */
    public void setBackground(boolean isBackground){
         this.isBackground=isBackground;
         connectionPool().changeHeartbeatInterval(heartInterval());
    }

    public static final class Builder{
     Dispatcher dispatcher;
     final List<Interceptor> interceptors = new ArrayList<>();
     int connectTimeout;
     int sendTimeout;
     int resendCount;
     boolean connectionRetryEnabled;
     int heartIntervalForeground;
     int heartIntervalBackground;
     boolean isBackground;
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     Cache cache;
     Authenticator authenticator;
     List<Address> addressList;
     @Nullable Codec codec;
     LinkedHashMap<String , ChannelHandler> channelHandlerLinkedHashMap;
     com.google.protobuf.GeneratedMessageV3 heartBeatMsg;
     MessageParser messageParser;
     Consumer ackConsumer;;

     public Builder(){
         dispatcher=new Dispatcher();
         heartIntervalForeground=3*1000;
         heartIntervalBackground=30*1000;
         isBackground=true;
         resendCount=3;
         sendTimeout=5*1000;
         connectTimeout=10*1000;
         connectionRetryEnabled=true;
         addressList=new ArrayList<>();
         this.connectionPool=new ConnectionPool();
         eventListenerFactory = EventListener.factory(EventListener.NONE);
         messageParser=new MessageParser();
     }

        public Builder setCodec(@Nullable Codec codec){
         this.codec=codec;
         return this;
        }

        public Builder setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder setConnectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout =  checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder setAddressList(List<Address> addressList) {
            this.addressList = addressList;
            return this;
        }

        public Builder setAddress(Address address) {
            if (!addressList.contains(address)){
                addressList.add(address);
            }
            return this;
        }
        public Builder eventListener(EventListener eventListener){
            if (eventListener == null) throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        public Builder setSendTimeout(long timeout, TimeUnit unit) {
            this.sendTimeout =  checkDuration("timeout", timeout, unit);;
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

        public Builder addInterceptor(Interceptor interceptor){
             interceptors.add(interceptor);
             return this;
        }
        public Builder addChannelHandler(String name,ChannelHandler channelHandler){
            channelHandlerLinkedHashMap.put(name,channelHandler);
            return this;
        }

        public Builder setConnectionRetryEnabled(boolean connectionRetryEnabled){
           this.connectionRetryEnabled=connectionRetryEnabled;
           return this;
        }

        /**
         * 注册消息处理器
         * @param messageHandler
         * @return
         */
        public Builder registerMessageHandler(MessageHandler messageHandler){
             messageParser.registerMessageHandler(messageHandler);
           return this;
        }

        public Builder setBackground(boolean isBackground){
         this.isBackground=isBackground;
         return this;
        }

        /**
         * 设置事件监听
         * @param eventListener
         * @return
         */
        public Builder setEventListener(EventListener eventListener){
         this.eventListenerFactory=EventListener.factory(eventListener);
         return this;
        }

        public Builder setShakeHands(MessageShakeHandsHandler shakeHandler){
         this.messageParser.registerMessageShakeHandsHandler(shakeHandler);
         return this;
        }

        public Builder setHeartBeatMsg(com.google.protobuf.GeneratedMessageV3 heartBeatMsg){
         this.heartBeatMsg=heartBeatMsg;
         return this;
        }

        /**
         * 设置ACK机制,如果设置了，在request里有needACK,则必须收到ACK包 不然会回调onFailure
         * @param ackConsumer
         * @return
         */
        public Builder setAckConsumer(Consumer ackConsumer){
         this.ackConsumer=ackConsumer;
         return this;
        }



        public IMClient build(){return new IMClient(this);}
    }

    private static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }
    private static void checkAddressList(List<Address> addressList){
        if (addressList==null||addressList.size()<1){
            throw new IllegalStateException("address == null");
        }
    }
}
