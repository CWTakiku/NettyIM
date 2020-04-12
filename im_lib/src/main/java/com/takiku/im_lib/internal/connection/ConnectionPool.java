package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.Codec.Codec;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.internal.handler.HeartbeatRespHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/11
 */
public final class ConnectionPool {

    private Bootstrap bootstrap;

    private static final ExecutorService bossPool = Executors.newFixedThreadPool(1);
    private static final ExecutorService workPool = Executors.newFixedThreadPool(1);;// 工作线程组，负责心跳
    private  RealConnection realConnection;
    public RouteDatabase routeDatabase = new RouteDatabase();



    public ConnectionPool(){
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // 设置禁用nagle算法
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }
    public void setConnectTimeout(int connectTimeout){
        // 设置连接超时时长
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
    }

    public void deduplicate(StreamAllocation streamAllocation){
        realConnection=null;
        if (streamAllocation!=null){
            streamAllocation.release();
        }
    }
    public void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        realConnection=connection;
    }
    public void ChannelInitializerHandler(Codec codec,com.google.protobuf.Internal.EnumLite heartResponseHandler){
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

                 pipeline.addLast(HeartbeatRespHandler.class.getSimpleName(),new HeartbeatRespHandler(heartResponseHandler));
                 pipeline.addLast()




             }
         });
    }

    /**
     * 执行boss任务
     *
     * @param r
     */
    public void execBossTask(Runnable r) {
        bossPool.execute(r);
    }

    /**
     * 执行work任务
     *
     * @param r
     */
    public void execWorkTask(Runnable r) {
        workPool.execute(r);
    }

    public RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
        if (realConnection!=null){
            streamAllocation.acquire(realConnection);
        }
        return realConnection;
    }
}
