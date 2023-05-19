package com.takiku.nettyim.customTcpClientDemo;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.Disposable;
import com.takiku.im_lib.defaultImpl.DefaultAckConsumer;
import com.takiku.im_lib.defaultImpl.DefaultReplyReceiveHandler;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.nettyim.callbcak.UICallback;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.codec.DefaultCodec;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.defaultImpl.DefaultHeartbeatRespHandler;
import com.takiku.im_lib.defaultImpl.DefaultMessageReceiveHandler;
import com.takiku.im_lib.defaultImpl.DefaultMessageShakeHandsHandler;
import com.takiku.im_lib.defaultImpl.DefaultEventListener;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.callbcak.UIConsumerCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.takiku.nettyim.Constants.MSG_ACK_TYPE;

public class IMClientDemo2 {
    private static IMClientDemo2 instance;
    private IMClient imClient;
    private Handler mHandler;
    private IMClientDemo2.OnMessageReceiveListener onMessageReceiveListener;
    private IMClientDemo2.OnReplyReceiveListener onReplyReceiveListener;


    public static final String userId2 = "userid2";

    private DefaultMessageReceiveHandler.onMessageArriveListener onMessageArriveListener=new DefaultMessageReceiveHandler.onMessageArriveListener() {
        @Override
        public void onMessageArrive(PackProtobuf.Pack pack) {
            final AppMessage appMessage=AppMessage.buildAppMessage(pack.getMsg());
            sendAck(appMessage.getHead().getMsgId(),pack.getMsg().getSerial(),pack.getMsg().getHead().getFromId());//发送ACK 给服务端
            if (onMessageReceiveListener!=null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onMessageReceiveListener.onMessageReceive(appMessage);
                    }
                });
            }
        }
    };
    public DefaultReplyReceiveHandler.OnReplyArriveListener onReplyListener=new DefaultReplyReceiveHandler.OnReplyArriveListener() {
        @Override
        public void onReplyArrive(PackProtobuf.Pack pack) {
            sendAck(pack.getReply().getMsgId(),pack.getReply().getSerial(),pack.getReply().getFromId());//发送ACK 给服务端
            final ReplyMessage replyMessage=ReplyMessage.buildReplyMessage(pack.getReply());
            if (onReplyReceiveListener!=null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onReplyReceiveListener.onReplyReceive(replyMessage);
                    }
                });
            }
        }
    };


    public void registerMessageReceive(IMClientDemo2.OnMessageReceiveListener onMessageReceiveListener){
        this.onMessageReceiveListener=onMessageReceiveListener;
    }
    public void unregisterMessageReceive(){
        this.onMessageReceiveListener=null;
    }

    public void registerReplyReceive(IMClientDemo2.OnReplyReceiveListener onReplyReceiveListener){
        this.onReplyReceiveListener=onReplyReceiveListener;
    }
    public void unregisterReplyReceive(){
        this.onReplyReceiveListener=null;
    }

    /**
     * IMCient
     * @param  //主要消息接受监听，
     */
    private IMClientDemo2(){

        mHandler=new Handler(Looper.getMainLooper());
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(new DefaultMessageShakeHandsHandler(getDefaultHands())) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setAckConsumer(new DefaultAckConsumer())
                .setConnectTimeout(10, TimeUnit.SECONDS)
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .registerMessageHandler(new DefaultMessageReceiveHandler(onMessageArriveListener)) //客户端消息接收处理器
                .registerMessageHandler(new DefaultReplyReceiveHandler(onReplyListener)) //消息状态接收处理器
                .registerMessageHandler(new DefaultHeartbeatRespHandler()) //心跳接收处理器
                .setEventListener(new DefaultEventListener(userId2)) //事件监听，可选
                .addAddress(new Address("192.168.31.223",9081,Address.Type.SOCKS))
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
     * 发送消息，回调在子线程 ,不需要回执
     * @param request
     * @param callback
     */
    public void sendMsg(Request request,Callback callback){
        imClient.newCall(request).enqueue(callback);
    }


    public long getMsgSerialID(){
        return imClient.getMsgSerialId();
    }
    /**
     * 发送ACK
     * @param msgId
     */
    public void sendAck(String msgId,long serial,String fromId){
        imClient.newCall(createAckRequest(msgId,serial,userId2,fromId)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {

            }
        });
    }

    private Request createAckRequest(String msgId,Long serial,String fromId,String toId) {
        return new  Request.Builder().setBody(getDefaultAck(msgId,serial,fromId,toId))
                .setNoNeedACK()
                .build();
    }

    /**
     * 发送消息回调在UI线程，不需要回执
     * @param request
     * @param callback
     */
    public void sendMsgUICallback(Request request,Callback callback){
        imClient.newCall(request).enqueue(new UICallback(callback,mHandler));
    }

    /**
     * 发送消息，会订阅特定的消息，
     * @param request
     * @param callback
     * @return
     */
    public Disposable sendMsgAndConsumer(Request request, Callback callback,Consumer ...consumers){
        List<Consumer> consumersList= Arrays.asList(consumers);
        List<Consumer> uiConsumerCallback=new ArrayList<>();
        for (Consumer consumer:consumersList){
            uiConsumerCallback.add(new UIConsumerCallback(consumer,mHandler));
        }
        Disposable disposable=   imClient.newCall(request).enqueue(new UICallback(callback,mHandler)).subscribe(uiConsumerCallback);
        return disposable;
    }


    /**
     * 构建默认心跳，开发者可自行定制自己的心跳包
     * @return
     */
    private GeneratedMessageV3 getDefaultHeart() {
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.HEART)
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId(userId2).build())
                .build();
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private  PackProtobuf.Pack getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("token2");
        shakeHandsMessage.setUserId(userId2);
        shakeHandsMessage.setMsgId("2222");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
    }

    /**
     * 默认确认包
     * @param msgId
     * @return
     */
    private PackProtobuf.Pack getDefaultAck(String msgId,Long serial,String fromId,String toId){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.ACK)
                .setAck(PackProtobuf.Ack.newBuilder().setAckMsgId(msgId)
                        .setAckType(MSG_ACK_TYPE)
                        .setResult(0)
                        .setFromId(fromId)
                        .setToId(toId)
                        .setSerial(serial)
                        .build())
                .build();
    }
    public interface OnMessageReceiveListener{
        void onMessageReceive(AppMessage appMessage);
    }
    public interface OnReplyReceiveListener{
        void  onReplyReceive(ReplyMessage replyMessage);
    }


}
