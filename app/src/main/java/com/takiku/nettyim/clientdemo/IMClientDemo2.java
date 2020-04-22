package com.takiku.nettyim.clientdemo;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.UICallback;
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

public class IMClientDemo2 {
    private static IMClientDemo2 instance;
    private IMClient imClient;
    private IMClientDemo2(){
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
    private IMClientDemo2(DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener){
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setMessageRespHandler(new DefaultMessageRespHandler()) //消息响应，开发者可自行定制实现MessageRespHandler接口即可
                .setMessageReceiveHandler(new DefaultMessageReceiveHandler(onMessageArriveListener))
                .setEventListener(new DefaultEventListener("user id2"))
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
                .setAddress(new Address("192.168.8.154",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
    }
    public static IMClientDemo2 getInstance(){
        if (instance==null){
            synchronized (IMClientDemo2.class){
                if (instance==null){
                    instance=new IMClientDemo2();
                }
            }
            return instance;
        }
        return instance;
    }
    public static IMClientDemo2 getInstance(DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener){
        if (instance==null){
            synchronized (IMClientDemo2.class){
                if (instance==null){
                    instance=new IMClientDemo2(onMessageArriveListener);
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

    /**
     * 设置前后台心跳包间隔切换
     * @param background
     */
    public void setBackground(boolean background){
        imClient.setBackground(background);
    }

    /**
     * 发送消息，回调在主线程
     * @param request
     * @param onResponseListener
     */
    public void sendMsg(Request request, UICallback.OnResultListener onResponseListener){
        imClient.newCall(request).enqueue(new UICallback(onResponseListener));
    }


    public void sendMsg(Request request, Callback callback){
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
    public void sendReplyUI(Request request){
        imClient.newCall(request).enqueue(new UICallback(null));
    }


    /**
     * 构建默认心跳，开发者可自行定制自己的心跳包
     * @return
     */
    private GeneratedMessageV3 getDefaultHeart() {
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.HEART)
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId("user id2").build())
                .build();
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private GeneratedMessageV3 getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("token2");
        shakeHandsMessage.setUserId("user id2");
        shakeHandsMessage.setMsgId("2222");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
    }

}
