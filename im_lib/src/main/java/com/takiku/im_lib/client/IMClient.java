package com.takiku.im_lib.client;

import androidx.annotation.Nullable;

import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.RealCall;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.dispatcher.Dispatcher;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.codec.DefaultCodec;
import com.takiku.im_lib.internal.Internal;
import com.takiku.im_lib.internal.connection.ConnectionPool;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.internal.handler.InternalChannelHandler;
import com.takiku.im_lib.internal.handler.MessageRespHandler;
import com.takiku.im_lib.internal.handler.ShakeHandsHandler;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;

public class IMClient {



    static {
        Internal.instance = new Internal(){

            @Override
            public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
                return pool.get(address, streamAllocation);
            }

            @Override
            public void deduplicate(ConnectionPool pool,  StreamAllocation streamAllocation) {
                 pool.deduplicate( streamAllocation);
            }

            @Override
            public void put(ConnectionPool pool, RealConnection connection) {
                pool.put(connection);
            }



        };
    }

     Dispatcher dispatcher;
     List<Interceptor> interceptors;
     private int resendCount;
     int connectTimeout;
     int sendTimeout;
     boolean connectionRetryEnabled;
     int resendInterval;// 重发间隔
     int heartIntervalForeground;
     int heartIntervalBackground;
     boolean isBackground;//是否处于后台
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     Bootstrap bootstrap;
     Codec codec;
     LinkedHashMap<String , ChannelHandler> customChannelHandlerLinkedHashMap;
     com.google.protobuf.GeneratedMessageV3 loginAuthMsg;
     com.google.protobuf.GeneratedMessageV3 heartBeatMsg;
     ShakeHandsHandler shakeHandsHandler;
     InternalChannelHandler heartChannelHandler;
     MessageRespHandler messageRespHandler;
     List<Address> addressList;

    public IMClient(){this(new Builder());}


    IMClient(Builder builder){
       this.resendCount=builder.resendCount;
       this.dispatcher=builder.dispatcher;
       this.connectTimeout=builder.connectTimeout;
       this.resendInterval=builder.resendInterval;
       this.heartIntervalBackground=builder.heartIntervalBackground;
       this.heartIntervalForeground=builder.heartIntervalForeground;
       this.connectionPool=builder.connectionPool;
       this.eventListenerFactory=builder.eventListenerFactory;
       this.connectionRetryEnabled=builder.connectionRetryEnabled;
       this.bootstrap=builder.bootstrap;
       this.codec=builder.codec;
       this.customChannelHandlerLinkedHashMap=builder.customChannelHandlerLinkedHashMap;
       this.loginAuthMsg=builder.loginAuthMsg;
       this.heartBeatMsg=builder.heartBeatMsg;
       this.shakeHandsHandler=builder.shakeHandsHandler;
       this.messageRespHandler =builder.messageRespHandler;
       this.heartChannelHandler=builder.heartChannelHandler;
       this.sendTimeout=builder.sendTimeout;
       this.addressList=builder.addressList;
       this.isBackground=builder.isBackground;
    }

    public void startConnect() {
        checkAddressList(addressList);
      Call call= newCall( new Request.Builder().setBody(null).build());
      call.enqueue(new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {

          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {

          }
      });
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public int resendCount(){return resendCount;}

    public ConnectionPool connectionPool(){return  connectionPool;};

    public Bootstrap bootstrap(){return bootstrap;}

    public Codec codec(){return codec;}

    public int connectTimeout(){
        return connectTimeout;
    }

    public int sendTimeout(){return sendTimeout;}

    public List<Address> addressList(){ return addressList; }

     public Call newCall(Request request) {
        return new RealCall(this, request);
    }

    public com.google.protobuf.GeneratedMessageV3 loginAuthMsg(){ return loginAuthMsg; }

    public com.google.protobuf.GeneratedMessageV3 heartBeatMsg(){ return heartBeatMsg; }

    public int heartInterval(){
        if (isBackground){
        return heartIntervalBackground;
        } else {
            return heartIntervalForeground;
        }
    }

    public  LinkedHashMap<String , ChannelHandler> customChannelHandlerLinkedHashMap(){ return customChannelHandlerLinkedHashMap; }

    public MessageRespHandler messageHandler(){
        return messageRespHandler;
    }

    public ShakeHandsHandler shakeHandsHandler(){
        return shakeHandsHandler;
    }

    public InternalChannelHandler heartChannelHandler(){
        return heartChannelHandler;
    }

    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }
    public boolean connectionRetryEnabled(){return connectionRetryEnabled;}

    public static final class Builder{
     Dispatcher dispatcher;
     final List<Interceptor> interceptors = new ArrayList<>();
     int connectTimeout;
     int sendTimeout;//发送超时
     int resendInterval;// 重发间隔
     int resendCount;//消息发送失败，重发次数
     boolean connectionRetryEnabled;//是否连接失败、连接重试
     int heartIntervalForeground;
     int heartIntervalBackground;
     boolean isBackground;
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     @Nullable Cache cache;
     Authenticator authenticator;
     List<Address> addressList;
     Bootstrap bootstrap;
     Codec codec;
     LinkedHashMap<String , ChannelHandler> customChannelHandlerLinkedHashMap;
     com.google.protobuf.GeneratedMessageV3 loginAuthMsg;
     com.google.protobuf.GeneratedMessageV3 heartBeatMsg;
     ShakeHandsHandler shakeHandsHandler;
     InternalChannelHandler heartChannelHandler;
     MessageRespHandler messageRespHandler;

     public Builder(){
         dispatcher=new Dispatcher();
         heartIntervalForeground=3*1000;
         heartIntervalBackground=30*1000;
         isBackground=true;
         resendInterval=0;
         resendCount=3;
         sendTimeout=5*1000;
         connectTimeout=10*1000;
         connectionRetryEnabled=true;
         addressList=new ArrayList<>();
         this.connectionPool=new ConnectionPool();
         eventListenerFactory = EventListener.factory(EventListener.NONE);
         codec=new DefaultCodec();
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


        public Builder setResendInterval(long interval, TimeUnit unit) {
            this.resendInterval = checkDuration("interval", interval, unit);
            return this;
        }

        public Builder setResendCount(int resendCount) {
            this.resendCount = resendCount;
            return this;
        }

        public Builder setHeartIntervalForeground(int heartIntervalForeground) {
            this.heartIntervalForeground = heartIntervalForeground;
            return this;
        }

        public Builder setHeartIntervalBackground(int heartIntervalBackground) {
            this.heartIntervalBackground = heartIntervalBackground;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor){
             interceptors.add(interceptor);
             return this;
        }

        public Builder setConnectionRetryEnabled(boolean connectionRetryEnabled){
           this.connectionRetryEnabled=connectionRetryEnabled;
           return this;
        }

        public Builder setBootstrap(Bootstrap bootstrap){
           this.bootstrap=bootstrap;
           return this;
        }
        public Builder setBackground(boolean isBackground){
         this.isBackground=isBackground;
         return this;
        }

        public Builder setShakeHands(com.google.protobuf.GeneratedMessageV3 shakeHands,ShakeHandsHandler shakeHandler){
         this.loginAuthMsg=shakeHands;
         this.shakeHandsHandler=shakeHandler;
         return this;
        }
        public Builder setMessageRespHandler(MessageRespHandler messageRespHandler){
         this.messageRespHandler = messageRespHandler;
         return this;
        }

        public Builder setHeartBeatMsg(com.google.protobuf.GeneratedMessageV3 heartBeatMsg){
         this.heartBeatMsg=heartBeatMsg;
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
