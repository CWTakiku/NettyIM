package com.takiku.im_lib.internal.connection;

import com.google.protobuf.GeneratedMessageV3;
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
        if (channel!=null&&channel.isActive()){
            System.out.println(" requestTag: "+request.requestTag+" request body: "+request.requestBody.toString());
            channel.writeAndFlush(request.requestBody);
        }

    }

    @Override
    public Response readResponse(Request request) {
        LRUMap<String,Object> lruMap= streamAllocation.connection().lruMap();
        if (lruMap.containsKey(request.requestTag)){
            Object object=lruMap.get(request.requestTag);
           return new Response.Builder().setCode(Response.SUCCESS).setRequest(request).setResponse((GeneratedMessageV3) object).build();
        }

        return null;
    }




}
