package com.takiku.im_lib.client;

import androidx.annotation.Nullable;

import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.dispatcher.Dispatcher;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.internal.Internal;
import com.takiku.im_lib.internal.connection.ConnectionPool;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.Route;
import com.takiku.im_lib.internal.connection.RouteDatabase;
import com.takiku.im_lib.internal.connection.StreamAllocation;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.protobuf.MessagePro;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class IMClient {



    static {
        Internal.instance = new Internal(){

            @Override
            public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation, Route route) {
                return pool.get(address, streamAllocation, route);
            }

            @Override
            public void deduplicate(ConnectionPool pool,  StreamAllocation streamAllocation) {
                 pool.deduplicate( streamAllocation);
            }

            @Override
            public void put(ConnectionPool pool, RealConnection connection) {
                pool.put(connection);
            }

            @Override
            public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
                return connectionPool.routeDatabase;
            }

            @Override
            public com.google.protobuf.Internal.EnumLite HeartPack() {
                return PackProtobuf.PackType.HEART;
            }
        };
    }

     Dispatcher dispatcher;
     List<Interceptor> interceptors;
     private int resendCount;
     int connectTimeout;
     boolean connectionRetryEnabled;
     int resendInterval;// 重发间隔
     int heartIntervalForeground;
     int heartIntervalBackground;
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     Bootstrap bootstrap;

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
     EventListener.Factory eventListenerFactory;
     ConnectionPool connectionPool;
     @Nullable Cache cache;
     Authenticator authenticator;
     List<Address> addressList;
     Bootstrap bootstrap;



     Builder(){
         dispatcher=new Dispatcher();
         heartIntervalForeground=3*1000;
         heartIntervalBackground=30*1000;
         resendInterval=0;
         resendCount=3;
         connectionRetryEnabled=true;
         addressList=new ArrayList<>();
         this.connectionPool=new ConnectionPool();
         eventListenerFactory = EventListener.factory(EventListener.NONE);
     }

        public Builder setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder setConnectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout =  checkDuration("timeout", timeout, unit);
             connectionPool.setConnectTimeout(connectTimeout);
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
}
