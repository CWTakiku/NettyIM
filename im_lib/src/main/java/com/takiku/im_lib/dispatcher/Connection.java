package com.takiku.im_lib.dispatcher;

import java.nio.channels.Channel;

public interface Connection {
    Route route();
    Channel channel();
    Handshake handshake();
}
