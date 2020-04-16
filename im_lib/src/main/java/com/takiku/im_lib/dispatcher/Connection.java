package com.takiku.im_lib.dispatcher;

import com.takiku.im_lib.internal.connection.Route;
import com.takiku.im_lib.internal.connection.TcpStream;

import io.netty.channel.Channel;


public interface Connection {

    Channel channel();
    Handshake handshake();
}
