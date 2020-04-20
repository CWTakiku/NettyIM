package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.exception.AuthException;
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
    public StreamAllocation(ConnectionPool connectionPool, List<Address> addressList, LinkedHashMap<String, ChannelHandler>  channelHandlerMap, Object callStackTrace){
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
        closeQuietly(connection);
    }

    private void closeQuietly(RealConnection connection) {
        released=true;
        if (connection!=null){
           connection.release();
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

    public TcpStream newStream(IMClient client, Interceptor.Chain chain, EventListener eventListener) throws IOException, InterruptedException,AuthException  {
        int connectTimeout = chain.connectTimeoutMillis();
        int heartbeatInterval= client.heartInterval();
        Address address=chain.request().address;
        synchronized (connectionPool) {
            if (released){  throw new IllegalStateException("released");}

            if (this.connection==null||!this.connection.isHealth()){
                Internal.instance.get(connectionPool, address, this);
                    if(connection==null||!connection.isHealth()){
                        connection= new RealConnection(connectionPool, routeSelector.lastInetSocketAddress(), eventListener);

                        connection.ChannelInitializerHandler(client.codec(), client.loginAuthMsg(), client.heartBeatMsg(),
                                client.shakeHandsHandler(), client.heartbeatRespHandler(),
                                client.messageRespHandler(),client.messageReceiveHandler(),
                                client.customChannelHandlerLinkedHashMap(), new RealConnection.connectionBrokenListener() {
                                    @Override
                                    public void connectionBroken() {
                                        closeQuietly(connection);
                                        client.startConnect();
                                    }
                                });
                        connection.connect(connectTimeout);
                        Internal.instance.put(connectionPool,connection);
                        TcpStream tcpStream=connection.newStream(client,this,client.heartInterval());
                        return tcpStream;
                    }else {return connection.newStream(client,this,heartbeatInterval);}
            }else {
                return connection.newStream(client,this,heartbeatInterval);
            }
        }
    }
    public synchronized RealConnection connection() {
        return connection;
    }



}
