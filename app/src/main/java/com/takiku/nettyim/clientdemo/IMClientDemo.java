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
import com.takiku.im_lib.internal.DefaultHeartbeatRespHandler;
import com.takiku.im_lib.internal.DefaultMessageReceiveHandler;
import com.takiku.im_lib.internal.DefaultMessageSubsequentHandler;
import com.takiku.im_lib.internal.DefaultMessageShakeHandsHandler;
import com.takiku.im_lib.listener.DefaultEventListener;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * author:chengwl
 * Description: IMClient使用demo， 所有的默认实现都可替换成开发者的实现
 * Date:2020/4/18
 */
public class IMClientDemo {

    private static IMClientDemo instance;
    private IMClient imClient;


    /**
     * IMCient
     * @param onMessageArriveListener //主要消息接受监听，单一职责
     */
    private IMClientDemo(DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener){
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(new DefaultMessageShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setConnectTimeout(10, TimeUnit.SECONDS)
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .setMessageSubsequentHandler(new DefaultMessageSubsequentHandler()) //消息响应，开发者可自行定制实现MessageRespHandler接口即可
                .registerMessageHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //客户端消息接收处理器
                .registerMessageHandler(new DefaultHeartbeatRespHandler()) //心跳接收处理器
                .setEventListener(new DefaultEventListener("user id1")) //事件监听，可选
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
    }
    public static IMClientDemo getInstance(){
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

    /**
     * 发送消息，回调在子线程
     * @param request
     * @param callback
     */
    public void sendMsg(Request request,Callback callback){
        imClient.newCall(request).enqueue(callback);
    }

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
     * 发送状态回复 在UI线程，这里与发送消息并无二异，只是request设置无需要服务端响应
     * @param request
     */
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
