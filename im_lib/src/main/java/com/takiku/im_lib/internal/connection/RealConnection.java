package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.dispatcher.Handshake;

import io.netty.channel.Channel;


public class RealConnection  implements Connection {

    private final Route route;
    private Channel channel;
    private final ConnectionPool connectionPool;

    RealConnection(ConnectionPool connectionPool, Route route){
        this.route=route;
        this.connectionPool=connectionPool;

    }

    @Override
    public Route route() {
        return route;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    public void connect( int connectTimeout){


    }

    @Override
    public Handshake handshake() {
        return null;
    }
}
