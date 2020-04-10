package com.takiku.im_lib.client;

import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.dispatcher.Dispatcher;
import com.takiku.im_lib.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class IMClient {

     Dispatcher dispatcher;
     List<Interceptor> interceptors;
     private Bootstrap bootstrap;

    public IMClient(){this(new Builder());}


    IMClient(Builder builder){

    }
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public static final class Builder{
     Dispatcher dispatcher;
     final List<Interceptor> interceptors = new ArrayList<>();
     Cache cache;
     Authenticator authenticator;
     int connectTimeout;
     int sendTimeout;//发送超时
     int resendInterval;// 重发间隔
     int resendCount;//消息发送失败，重发次数
     int heartIntervalForeground;
     int heartIntervalBackground;
     private Bootstrap bootstrap;



     Builder(){
         dispatcher=new Dispatcher();
         heartIntervalForeground=3*1000;
         heartIntervalBackground=30*1000;
         resendInterval=0;
         resendCount=3;
         bootstrap=initBootstrap();
     }

        private Bootstrap initBootstrap() {
            EventLoopGroup loopGroup = new NioEventLoopGroup(4);
            bootstrap = new Bootstrap();
            bootstrap.group(loopGroup).channel(NioSocketChannel.class);
            // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            // 设置禁用nagle算法
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // 设置连接超时时长
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
            return bootstrap;
        }

        public Builder setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder setConnectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout =  checkDuration("timeout", timeout, unit);
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
