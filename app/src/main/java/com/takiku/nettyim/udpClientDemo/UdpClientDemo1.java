package com.takiku.nettyim.udpClientDemo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.Disposable;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.codec.DefaultCodec;
import com.takiku.im_lib.codec.DefaultUdpStringCodec;
import com.takiku.im_lib.defaultImpl.DefaultEventListener;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.HeartbeatMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.nettyim.callbcak.UICallback;
import com.takiku.nettyim.callbcak.UIConsumerCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import static com.takiku.nettyim.Constants.MSG_ACK_TYPE;

/**
 * @author chengwl
 * @des
 * @Date:2023/5/16
 */
public class UdpClientDemo1 {

    private static UdpClientDemo1 instance;
    private IMClient imClient;
    private Handler mHandler;
    private OnMessageReceiveListener onMessageReceiveListener;
    private OnReplyReceiveListener onReplyReceiveListener;
    public static final String userId1 = "userid1";
    private static final String ip = "192.168.31.212";

   private UdpMessageReceiveHandler.onMessageArriveListener onMessageArriveListener = new UdpMessageReceiveHandler.onMessageArriveListener() {
       @Override
       public void onMessageArrive(String pack) {
           final AppMessage appMessage= new Gson().fromJson( pack,AppMessage.class);
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
 private UdpMessageReplyHandler.OnReplyArriveListener onReplyListener = new UdpMessageReplyHandler.OnReplyArriveListener() {
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


    public void registerMessageReceive(OnMessageReceiveListener onMessageReceiveListener){
        this.onMessageReceiveListener=onMessageReceiveListener;
    }
    public void unregisterMessageReceive(){
        this.onMessageReceiveListener=null;
    }

    public void registerReplyReceive(OnReplyReceiveListener onReplyReceiveListener){
        this.onReplyReceiveListener=onReplyReceiveListener;
    }
    public void unregisterReplyReceive(){
        this.onReplyReceiveListener=null;
    }

    /**
     * IMCient
     * @param  //主要消息接受监听，
     */
    private UdpClientDemo1(){
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        mHandler=new Handler(Looper.getMainLooper());
        imClient=new IMClient.Builder()
                .setShakeHands(new UdpMessageShakeHandsHandler(getDefaultHands())) //设置握手认证，可选
                .setCodec(new DefaultUdpStringCodec(new InetSocketAddress(ip,8804), CharsetUtil.UTF_8)) //string 编解码
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setAckConsumer(new UdpAckConsumer()) //设置确认机制
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectRetryInterval(1000,TimeUnit.MILLISECONDS)
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .registerMessageHandler(new UdpMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                .registerMessageHandler(new UdpMessageReplyHandler(onReplyListener)) //消息状态接收处理器
                .registerMessageHandler(new UdpHeartbeatRespHandler()) //心跳接收处理器
                .setEventListener(new DefaultEventListener(userId1)) //事件监听，可选
                // .addAddress(new Address("192.168.31.212",9081,Address.Type.SOCKS))
                .addAddress(new Address(ip,8804,Address.Type.UDP))
                .setOpenLog(true)
                .setProtocol(IMProtocol.UDP)
                .build();
    }

    private String getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage = new ShakeHandsMessage();
        shakeHandsMessage.setUserId(userId1);
        shakeHandsMessage.setToken("token"+userId1);
     //   DatagramPacket datagramPacket = new DatagramPacket(Unpooled.copiedBuffer(new Gson().toJson(shakeHandsMessage),StandardCharsets.UTF_8),new InetSocketAddress(ip,8804));
        return new Gson().toJson(shakeHandsMessage);
    }

    public static UdpClientDemo1 getInstance(){
        if (instance==null){
            synchronized (UdpClientDemo1.class){
                if (instance==null){
                    instance=new UdpClientDemo1();
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
    private String getDefaultHeart() {
        HeartbeatMessage heartbeatMessage  = new HeartbeatMessage();
        String heart = new Gson().toJson(heartbeatMessage);
        return heart;
    }



    /**
     * 默认确认包
     * @param msgId
     * @return
     */
    private String getDefaultAck(String msgId,String fromId,String toId){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setMsgId(msgId);
        ackMessage.setAckType(MSG_ACK_TYPE);
      return   new Gson().toJson(ackMessage);
    }
    public interface OnMessageReceiveListener{
        void onMessageReceive(AppMessage appMessage);
    }
    public interface OnReplyReceiveListener{
        void  onReplyReceive(ReplyMessage replyMessage);
    }

}
