package com.takiku.im_lib.dispatcher;

import java.nio.channels.Channel;

public class RealConnection  implements Connection{

    private final Route route;
    private Channel channel;

    RealConnection( Route route){
        this.route=route;
    }

    @Override
    public Route route() {
        return route;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public Handshake handshake() {
        return null;
    }
}
