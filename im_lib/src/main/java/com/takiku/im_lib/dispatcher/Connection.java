package com.takiku.im_lib.dispatcher;

import java.io.Serializable;
import java.net.InetSocketAddress;

import io.netty.channel.Channel;


public interface Connection {

    Channel channel();
    InetSocketAddress InetSocketAddress();
    Serializable generateNetId();
}
