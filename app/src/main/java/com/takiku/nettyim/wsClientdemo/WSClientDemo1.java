package com.takiku.nettyim.wsClientdemo;

import static com.takiku.nettyim.Constants.MSG_ACK_TYPE;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.Disposable;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.defaultImpl.DefaultAckConsumer;
import com.takiku.im_lib.defaultImpl.DefaultCodec;
import com.takiku.im_lib.defaultImpl.DefaultEventListener;
import com.takiku.im_lib.defaultImpl.DefaultHeartbeatRespHandler;
import com.takiku.im_lib.defaultImpl.DefaultMessageReceiveHandler;
import com.takiku.im_lib.defaultImpl.DefaultMessageShakeHandsHandler;
import com.takiku.im_lib.defaultImpl.DefaultReplyReceiveHandler;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.nettyim.callbcak.UICallback;
import com.takiku.nettyim.callbcak.UIConsumerCallback;
import com.takiku.nettyim.customTcpClientdemo.IMClientDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class WSClientDemo1 {

    private static WSClientDemo1 instance;
    private IMClient imClient;
    private Handler mHandler;
    private WSClientDemo1.OnMessageReceiveListener onMessageReceiveListener;
    private WSClientDemo1.OnReplyReceiveListener onReplyReceiveListener;
    public static final String userId1 = "userid1";

   private WSMessageReceiveHandler.onMessageArriveListener onMessageArriveListener = new WSMessageReceiveHandler.onMessageArriveListener() {
       @Override
       public void onMessageArrive(TextWebSocketFrame pack) {
           Log.i("WSClientDemo1",pack.text());
           final AppMessage appMessage= new Gson().fromJson(pack.text(),AppMessage.class);
           sendAck(appMessage.getHead().getMsgId(),appMessage.getHead().getFromId());//发送ACK 给服务端
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
 private WSMessageReplyHandler.OnReplyArriveListener onReplyListener = new WSMessageReplyHandler.OnReplyArriveListener() {
     @Override
     public void onReplyArrive(ReplyMessage pack) {
         sendAck(pack.getMsgId(),pack.getFromId());//发送ACK 给服务端
         if (onReplyReceiveListener!=null){
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     onReplyReceiveListener.onReplyReceive(pack);
                 }
             });
         }
     }
 };


    public void registerMessageReceive(WSClientDemo1.OnMessageReceiveListener onMessageReceiveListener){
        this.onMessageReceiveListener=onMessageReceiveListener;
    }
    public void unregisterMessageReceive(){
        this.onMessageReceiveListener=null;
    }

    public void registerReplyReceive(WSClientDemo1.OnReplyReceiveListener onReplyReceiveListener){
        this.onReplyReceiveListener=onReplyReceiveListener;
    }
    public void unregisterReplyReceive(){
        this.onReplyReceiveListener=null;
    }

    /**
     * IMCient
     * @param  //主要消息接受监听，
     */
    private WSClientDemo1(){

        mHandler=new Handler(Looper.getMainLooper());
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
               // .setShakeHands(new DefaultMessageShakeHandsHandler(getDefaultHands())) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setAckConsumer(new WSAckConsumer()) //设置心跳机制
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .registerMessageHandler(new WSMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                .registerMessageHandler(new WSMessageReplyHandler(onReplyListener)) //消息状态接收处理器
               // .registerMessageHandler(new DefaultHeartbeatRespHandler()) //心跳接收处理器
                .setEventListener(new DefaultEventListener(userId1)) //事件监听，可选
                // .addAddress(new Address("192.168.31.212",9081,Address.Type.SOCKS))
                .addAddress(new Address("ws://192.168.31.218:8804/ws",8804,Address.Type.WS))
                .addWsHeader("user",userId1)
                .setProtocol(IMProtocol.WEB_SOCKET)
                .build();
    }

    public static WSClientDemo1 getInstance(){
        if (instance==null){
            synchronized (WSClientDemo1.class){
                if (instance==null){
                    instance=new WSClientDemo1();
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
    public void sendMsg(Request request, Callback callback){
        imClient.newCall(request).enqueue(callback);
    }


    public long getMsgSerialID(){
        return imClient.getMsgSerialId();
    }

    /**
     * 收到服务端消息后马上发送ACK
     * @param msgId
     */
    public void sendAck(String msgId,String toId){
        imClient.newCall(createAckRequest(msgId,userId1,toId)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {

            }
        });
    }

    private Request createAckRequest(String msgId,String fromId,String toId) {

        return new  Request.Builder().setBody(getDefaultAck(msgId,fromId,toId))
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
     * 发送消息，会订阅特定的消息
     * @param request
     * @param callback
     * @return Disposable 不需要订阅了 一定要调用 Disposable.disposable()
     */
    public Disposable sendMsgAndConsumer(Request request, Callback callback, Consumer...consumers){
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
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId(userId1).build())
                .build();
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private  PackProtobuf.Pack getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("token1");
        shakeHandsMessage.setUserId(userId1);
        shakeHandsMessage.setMsgId("1111");
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
    private AckMessage getDefaultAck(String msgId,String fromId,String toId){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setMsgId(msgId);
      return   ackMessage;
    }
    public interface OnMessageReceiveListener{
        void onMessageReceive(AppMessage appMessage);
    }
    public interface OnReplyReceiveListener{
        void  onReplyReceive(ReplyMessage replyMessage);
    }

}
