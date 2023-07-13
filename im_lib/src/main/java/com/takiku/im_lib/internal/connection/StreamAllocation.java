package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.exception.ConnectionShutdownException;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.internal.Internal;
import com.takiku.im_lib.listener.EventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.List;

import io.netty.channel.ChannelHandler;

public class StreamAllocation {

    private final List<Address> addressList;
    private final Object callStackTrace;
    private RealConnection connection;
    private final RouteSelector routeSelector; //路由选择器
    private boolean released;
    private boolean canceled;
    private ConnectionPool connectionPool;
    private LinkedHashMap<String, ChannelHandler> channelHandlerMap;
    public StreamAllocation(ConnectionPool connectionPool, List<Address> addressList,LinkedHashMap<String, ChannelHandler>  channelHandlerMap, Object callStackTrace){
      this.addressList = addressList;
      this.connectionPool=connectionPool;
      this.callStackTrace=callStackTrace;
      this.routeSelector=new RouteSelector(addressList);
      this.channelHandlerMap=channelHandlerMap;
    }

    public void nextRoute() throws IOException {
        routeSelector.nextInetSocketAddress();
    }
    public InetSocketAddress currentInetSocketAddress(){
        return routeSelector.lastInetSocketAddress();
    }

    public void release() {
      release(true);
    }
    public void release(boolean isReConnect) {
        closeQuietly(connection,isReConnect);
    }

    private void closeQuietly(RealConnection connection) {
       closeQuietly(connection,true);
    }
    private void closeQuietly(RealConnection connection,boolean isReConnect) {
        released=true;
        if (connection!=null){
            connection.release(isReConnect);
            connection=null;
        }
    }


    public void acquire(RealConnection connection) {
        if (this.connection != null&&this.connection.isHealth()) throw new IllegalStateException();

        this.connection = connection;
    }


    public boolean hasMoreRoutes() {
        return routeSelector.hasNext();
    }

    public IStream newStream(IMClient client, Interceptor.Chain chain, EventListener eventListener) throws IOException, InterruptedException,AuthException  {
        int connectTimeout = chain.connectTimeoutMillis();
        int heartbeatInterval= client.heartInterval();
        int readerIdleTime = client.readerIdleTime();
        boolean readerIdleReconnectEnabled = client.readerIdleReconnectEnabled();
        Address address=chain.request().address;
        synchronized (connectionPool) {
            if (released){  throw new IllegalStateException("released");}

            if (this.connection==null||!this.connection.isHealth()){
                Internal.instance.get(connectionPool, address, this);
                    if(connection==null||!connection.isHealth()){
                        if (!client.msgTriggerReconnectEnabled()&&!(chain.request() instanceof ConnectRequest)){
                            throw new ConnectionShutdownException();
                        }
                        connection= new RealConnection(connectionPool, routeSelector.lastInetSocketAddress(),client.protocol(),client.port(), eventListener);
                        connection.ChannelInitializerHandler(client.codec(),client.frameCodec(),client.wsHeaderMap(),client.heartBeatMsg(),
                                client.customChannelHandlerLinkedHashMap(),heartbeatInterval,readerIdleTime,
                                readerIdleReconnectEnabled,client.messageParser()
                                ,new RealConnection.connectionBrokenListener() {
                                    @Override
                                    public void connectionBroken() {

                                        if (connection.isReConnect()){
                                            closeQuietly(connection);
                                            client.startConnect();
                                        }else {
                                            closeQuietly(connection);
                                        }

                                    }
                                },client.lengthFieldLength(),client.maxFrameLength());
                        connection.connect(connectTimeout,client.port());
                        Internal.instance.put(connectionPool,connection);
                        IStream iStream =connection.newStream(client,this);
                        return iStream;
                    }else {return connection.newStream(client,this);}
            }else {
                return connection.newStream(client,this);
            }
        }
    }
    public synchronized RealConnection connection() {
        return connection;
    }



}
