package com.takiku.im_lib.dispatcher;

import io.netty.channel.Channel;


public interface Connection {

    Channel channel();
    Handshake handshake();
}
