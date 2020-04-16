package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.util.LRUMap;

import java.io.IOException;

import io.netty.channel.Channel;


public class Stream implements TcpStream {

    final IMClient imClient;
    final StreamAllocation streamAllocation;
    final Channel channel;

    public Stream(IMClient imClient, StreamAllocation streamAllocation, Channel channel){
       this.imClient=imClient;
       this.streamAllocation=streamAllocation;
       this.channel=channel;
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public void cancel() {
        streamAllocation.release();
    }

    @Override
    public void writeRequest(Request request) throws IOException {
        if (channel!=null&&channel.isActive())
        channel.writeAndFlush(request.getValue());
    }

    @Override
    public Response readResponse(Request request) {
        LRUMap<Integer,Object> liteLRUMap= streamAllocation.connection().lruMap();

        return null;
    }




}
