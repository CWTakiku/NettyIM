package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.Codec.Codec;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.Address;
import com.takiku.im_lib.interceptor.Interceptor;
import com.takiku.im_lib.internal.Internal;

import java.io.IOException;

public class StreamAllocation {

    private final Address address;
    private final Object callStackTrace;
    private RealConnection connection;
    private final RouteSelector routeSelector; //路由选择器
    private Route route;
    private ConnectionPool connectionPool;
    public StreamAllocation(ConnectionPool connectionPool, Address address, Object callStackTrace){
      this.address = address;
      this.connectionPool=connectionPool;
      this.callStackTrace=callStackTrace;
      this.routeSelector=new RouteSelector(address, routeDatabase());
    }

    private RouteDatabase routeDatabase() {
       return connectionPool.routeDatabase;
    }

    public void release() {
        closeQuietly(connection);
    }

    private void closeQuietly(RealConnection connection) {

    }

    public void acquire(RealConnection connection) {
        if (this.connection != null) throw new IllegalStateException();

        this.connection = connection;
    }

    public void streamFailed(IOException e) {
    }

    public boolean hasMoreRoutes() {
        return route!=null||routeSelector.hasNext();
    }

    public Codec newStream(IMClient client, Interceptor.Chain chain) throws IOException {
        Route selectedRoute=null;
        int connectTimeout = chain.connectTimeoutMillis();
        int sendTimeout = chain.sendTimeoutMillis();
        synchronized (connectionPool) {
            if (this.connection==null){
                Internal.instance.get(connectionPool, address, this, null);
                if (connection==null){
                    if (selectedRoute == null) {
                            selectedRoute = routeSelector.next();
                    }
                    connectionPool
                    connection= new RealConnection(connectionPool, selectedRoute);
                    connection.
                }
            }else {

            }
        }

    }
    public synchronized RealConnection connection() {
        return connection;
    }
}
