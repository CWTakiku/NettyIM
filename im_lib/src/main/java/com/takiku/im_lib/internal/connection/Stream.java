package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.im_lib.util.LRUMap;
import com.takiku.im_lib.util.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;


public class Stream implements IStream {

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
        LogUtil.i("test","asdasd");
        if (channel!=null&&channel.isActive()){
            LogUtil.i("test","asdas1111d");
            if (IMProtocol.WEB_SOCKET == imClient.protocol()){
                if (request.requestBody instanceof TextWebSocketFrame){
                    channel.writeAndFlush(((TextWebSocketFrame) request.requestBody).retain());
                }
            } else if (IMProtocol.UDP == imClient.protocol()){
//                InetSocketAddress targetAddress = null;
//                if (request.address!=null){
//                    targetAddress = new InetSocketAddress(request.address.getUrl(),request.address.getPort());
//                }else {
//                    targetAddress = streamAllocation.currentInetSocketAddress();
//                }
                channel.writeAndFlush(request.requestBody);
            }else {
                channel.writeAndFlush(request.requestBody);
            }

        }

    }


    @Override
    public void registerConsumers(Request request, List<Consumer> consumers) {
        streamAllocation.connection().registerConsumer(request,consumers);
    }

    @Override
    public void registerAckConsumer(Request request) {
        streamAllocation.connection().registerAckConsumer(request,imClient.ackConsumer());
    }



//    @Override
//    public Response readAck(Request request) {
//        LRUMap<String,Object> lruMap= streamAllocation.connection().ackMsgLruMap();
//        if (lruMap.containsKey(request.requestTag)){
//            Object object=lruMap.get(request.requestTag);
//           return new Response.Builder().setCode(Response.SUCCESS).setRequest(request).setResponse((GeneratedMessageV3) object).build();
//        }
//
//        return null;
//    }
//
//    @Override
//    public void subsequentResponse(Request request, List<Consumer> consumers) {
//         streamAllocation.connection().registerSubsequentResponse(request.requestTag, new RealConnection.OnSubsequentResponseListener() {
//             @Override
//             public void onSubsequentResponseArrive(String tag, Object o) {
//                 Response response=   new Response.Builder().setCode(Response.SUCCESS).setRequest(request).setResponse((GeneratedMessageV3) o).build();
//                 callback.onSubsequentResponse(response);
//             }
//
//        });
//    }

    @Override
    public Response readResponse(Request request) {
        LRUMap<String,Response> lruMap= streamAllocation.connection().responseLRUMap();
        if (lruMap.containsKey(request.requestTag)){
            return lruMap.get(request.requestTag);
        }else {
            return null;
        }
    }

}
