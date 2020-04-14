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



    private static final ExecutorService bossPool = Executors.newFixedThreadPool(1);
    private static final ExecutorService workPool = Executors.newFixedThreadPool(1);;// 工作线程组，负责心跳
    private  RealConnection realConnection;
    public RouteDatabase routeDatabase = new RouteDatabase();



    public ConnectionPool(){

    }
    public void setConnectTimeout(int connectTimeout){
        // 设置连接超时时长
       // bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
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
