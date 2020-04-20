package com.takiku.nettyim.clientdemo;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.codec.DefaultCodec;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.DefaultMessageReceiveHandler;
import com.takiku.im_lib.internal.DefaultMessageRespHandler;
import com.takiku.im_lib.internal.DefaultShakeHandsHandler;
import com.takiku.im_lib.listener.DefaultEventListener;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

/**
 * author:chengwl
 * Description: IMClient使用demo， 所有的默认实现都可替换成开发者的实现
 * Date:2020/4/18
 */
public class IMClientDemo {

    private static IMClientDemo instance;
    private IMClient imClient;
    private IMClientDemo(){
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setMessageRespHandler(new DefaultMessageRespHandler()) //消息响应，开发者可自行定制实现MessageRespHandler接口即可
             //   .setMessageReceiveHandler(new DefaultMessageReceiveHandler())
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
                .setAddress(new Address("192.168.8.154",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
    }
    private IMClientDemo(DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener){
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setMessageRespHandler(new DefaultMessageRespHandler()) //消息响应，开发者可自行定制实现MessageRespHandler接口即可
                .setMessageReceiveHandler(new DefaultMessageReceiveHandler(onMessageArriveListener))
                .setEventListener(new DefaultEventListener())
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
                .setAddress(new Address("192.168.8.154",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
    }
    public static IMClientDemo getInstance(){
        if (instance==null){
            synchronized (IMClientDemo.class){
                if (instance==null){
                    instance=new IMClientDemo();
                }
            }
            return instance;
        }
        return instance;
    }
    public static IMClientDemo getInstance(DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener){
        if (instance==null){
            synchronized (IMClientDemo.class){
                if (instance==null){
                    instance=new IMClientDemo(onMessageArriveListener);
                }
            }
            return instance;
        }
        return instance;
    }


    /**
     * 建立连接
     */
    public void startConnect(){
        imClient.startConnect();
    }

    /**
     * 断开连接
     */
    public void disConnect(){imClient.disConnect();}

    public void sendMsg(Request request,Callback callback){
        imClient.newCall(request).enqueue(callback);
    }
    public void sendReply(Request request){
        imClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }



    /**
     * 构建默认心跳，开发者可自行定制自己的心跳包
     * @return
     */
    private GeneratedMessageV3 getDefaultHeart() {
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.HEART)
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId("user id1").build())
                .build();
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private GeneratedMessageV3 getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("token1");
        shakeHandsMessage.setUserId("user id1");
        shakeHandsMessage.setMsgId("1111");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
    }


}
