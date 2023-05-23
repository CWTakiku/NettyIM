package com.takiku.nettyim;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.Disposable;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.codec.DefaultTcpProtobufCodec;
import com.takiku.im_lib.codec.DefaultTcpStringCodec;
import com.takiku.im_lib.codec.DefaultUdpStringCodec;
import com.takiku.im_lib.defaultImpl.protobuf.DefaultProtobufAckConsumer;
import com.takiku.im_lib.defaultImpl.callback.DefaultEventListener;
import com.takiku.im_lib.defaultImpl.protobuf.DefaultProtobufHeartbeatRespHandler;
import com.takiku.im_lib.defaultImpl.protobuf.DefaultProtobufMessageReceiveHandler;
import com.takiku.im_lib.defaultImpl.protobuf.DefaultProtobufMessageShakeHandsHandler;
import com.takiku.im_lib.defaultImpl.protobuf.DefaultReplyReceiveHandler;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.HeartbeatMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.im_lib.defaultImpl.callback.OnMessageArriveListener;
import com.takiku.im_lib.defaultImpl.callback.OnMessageReceiveListener;
import com.takiku.im_lib.defaultImpl.callback.OnReplyArriveListener;
import com.takiku.im_lib.defaultImpl.callback.OnReplyReceiveListener;
import com.takiku.nettyim.callbcak.UICallback;
import com.takiku.nettyim.callbcak.UIConsumerCallback;
import com.takiku.im_lib.defaultImpl.string.DefaultStringAckConsumer;
import com.takiku.im_lib.defaultImpl.string.DefaultStringHeartbeatRespHandler;
import com.takiku.im_lib.defaultImpl.string.DefaultStringMessageReceiveHandler;
import com.takiku.im_lib.defaultImpl.string.DefaultStringMessageReplyHandler;
import com.takiku.im_lib.defaultImpl.string.DefaultStringMessageShakeHandsHandler;
import com.takiku.im_lib.defaultImpl.textWebSocketFrame.DefaultWSAckConsumer;
import com.takiku.im_lib.defaultImpl.textWebSocketFrame.DefaultWSMessageReceiveHandler;
import com.takiku.im_lib.defaultImpl.textWebSocketFrame.DefaultWSMessageReplyHandler;
import com.takiku.im_lib.defaultImpl.textWebSocketFrame.DefaultWsHeartbeatRespHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import static com.takiku.im_lib.util.Constants.MSG_ACK_TYPE;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/22
 */
public class Client {
    private IMClient imClient;
    private Handler mHandler;
    private OnMessageReceiveListener onMessageReceiveListener;
    private OnReplyReceiveListener onReplyReceiveListener;
    private String userId;
    private String ip;

    private @IMProtocol int protocol;



    private OnMessageArriveListener onMessageArriveListener = new OnMessageArriveListener() {
        @Override
        public void onMessageArrive(AppMessage appMessage) {
            sendAck(appMessage.getHead().getMsgId(),appMessage.getHead().getSerial(),appMessage.getHead().getFromId());//发送ACK 给服务端
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
    private OnReplyArriveListener onReplyListener = new OnReplyArriveListener() {
        @Override
        public void onReplyArrive(ReplyMessage pack) {
            sendAck(pack.getMsgId(),pack.getSerial(),pack.getFromId());//发送ACK 给服务端
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
    public Client(String ip, String userId, @IMProtocol int protocol,int codecType, EventListener eventListener){
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        mHandler=new Handler(Looper.getMainLooper());
        this.userId = userId;
        this.ip  = ip ;
        this.protocol = protocol;


        IMClient.Builder builder = new IMClient.Builder()
                .setConnectTimeout(10, TimeUnit.SECONDS) //设置连接超时
                .setResendCount(3)//设置失败重试数
                .setConnectRetryInterval(1000,TimeUnit.MILLISECONDS)
                .setConnectionRetryEnabled(true)//是否连接重试
                .setSendTimeout(6,TimeUnit.SECONDS)//设置发送超时
                .setHeartIntervalBackground(30,TimeUnit.SECONDS)//后台心跳间隔
                .setEventListener(eventListener!=null?eventListener:new DefaultEventListener(userId)) //事件监听，可选
                .setMsgTriggerReconnectEnabled(true)
                .setProtocol(protocol)
                .setOpenLog(true);

        if (protocol == IMProtocol.PRIVATE){
            builder.setCodec(codecType == 0?new DefaultTcpProtobufCodec():new DefaultTcpStringCodec())//默认的编解码，开发者可以使用自己的protobuf或者其他格式的编解码
                    .setShakeHands(codecType == 0? new DefaultProtobufMessageShakeHandsHandler(getDefaultTcpHands()):new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //设置握手认证，可选
                    .setHeartBeatMsg(codecType == 0? getDefaultProtobufHeart(): getDefaultStringHeart()) //设置心跳,可选
                    .setAckConsumer(codecType == 0?new DefaultProtobufAckConsumer():new DefaultStringAckConsumer()) //设置消息确认机制
                    .registerMessageHandler(codecType == 0?new DefaultProtobufMessageReceiveHandler(onMessageArriveListener):new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                    .registerMessageHandler(codecType == 0?new DefaultReplyReceiveHandler(onReplyListener):new DefaultStringMessageReplyHandler(onReplyListener)) //消息状态接收处理器
                    .registerMessageHandler(codecType == 0?new DefaultProtobufHeartbeatRespHandler():new DefaultStringHeartbeatRespHandler()) //心跳接收处理器
                    .addAddress(new Address(ip,9081,Address.Type.TCP))
                    .setMaxFrameLength(65535*100); //设置最大帧长 //私有tcp和websocket生效

        } else if (protocol == IMProtocol.WEB_SOCKET) {  //websocket比较特殊，不需要设置编解码和自定义握手消息，在内置的WebSocketClientHandshaker已经帮我们做了
            builder.setHeartBeatMsg(getDefaultWsHeart())
                    .setAckConsumer(new DefaultWSAckConsumer())
                    .registerMessageHandler(new DefaultWSMessageReceiveHandler(onMessageArriveListener))
                    .registerMessageHandler(new DefaultWSMessageReplyHandler(onReplyListener))
                    .registerMessageHandler(new DefaultWsHeartbeatRespHandler())
                    .addAddress(new Address(ip,8804,Address.Type.WS))
                    .setMaxFrameLength(65535*100)
                  //  .addAddress(new Address(ip,8804,Address.Type.WSS))//支持WSS协议，请在scheme带上wss标识
                    .addWsHeader("user",userId); //webSocket特有的

        }else if (protocol == IMProtocol.UDP) {
            builder.setCodec(new DefaultUdpStringCodec(new InetSocketAddress(ip,8804), CharsetUtil.UTF_8)) //UDP报文和String的编解码，开发者可以设定为自己的格式
                    .setShakeHands(new DefaultStringMessageShakeHandsHandler(getDefaultStringHands())) //设置握手认证，可选
                    .setHeartBeatMsg(getDefaultStringHeart()) //设置心跳,可选
                    .setAckConsumer(new DefaultStringAckConsumer()) //设置确认机制
                    .registerMessageHandler(new DefaultStringMessageReceiveHandler(onMessageArriveListener)) //消息接收处理器
                    .registerMessageHandler(new DefaultStringMessageReplyHandler(onReplyListener)) //消息状态接收处理器
                    .registerMessageHandler(new DefaultStringHeartbeatRespHandler()) //心跳接收处理器
                    .addAddress(new Address(ip, 8804, Address.Type.UDP));
        }

        imClient = builder.build();

    }


    private String getDefaultStringHands() {
        ShakeHandsMessage shakeHandsMessage = new ShakeHandsMessage();
        shakeHandsMessage.setUserId(userId);
        shakeHandsMessage.setToken("token"+userId.substring(userId.length()-1));
        return new Gson().toJson(shakeHandsMessage);
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private  PackProtobuf.Pack getDefaultTcpHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        if (userId.equals("userid1")){
            shakeHandsMessage.setToken("token1");
        }else {
            shakeHandsMessage.setToken("token2");
        }
        shakeHandsMessage.setUserId(userId);
        shakeHandsMessage.setMsgId("1111");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
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
    public void sendAck(String msgId, long serial,String toId){
        imClient.newCall(createAckRequest(msgId,serial,userId,toId)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {

            }
        });
    }

    private Request createAckRequest(String msgId,long serial,String fromId,String toId) {

        Object ack = null;
        if (IMProtocol.PRIVATE == protocol){
            return new  Request.Builder().setBody(getDefaultTcpAck(msgId,serial,fromId,toId))
                    .setNoNeedACK()
                    .build();
        }else if (IMProtocol.WEB_SOCKET == protocol){
            return new  Request.Builder().setBody(getDefaultWsAck(msgId,fromId,toId))
                    .setNoNeedACK()
                    .build();
        }else if (IMProtocol.UDP == protocol){
            return new  Request.Builder().setBody(getDefaultUdpAck(msgId,fromId,toId))
                    .setNoNeedACK()
                    .build();
        }
        return null;

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
    private String getDefaultStringHeart() {
        HeartbeatMessage heartbeatMessage  = new HeartbeatMessage();
        String heart = new Gson().toJson(heartbeatMessage);
        return heart;
    }

    /**
     * 构建默认心跳，开发者可自行定制自己的心跳包
     * @return
     */
    private GeneratedMessageV3 getDefaultProtobufHeart() {
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.HEART)
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId(userId).build())
                .build();
    }

    /**
     * 构建默认心跳，开发者可自行定制自己的心跳包
     * @return
     */
    private TextWebSocketFrame getDefaultWsHeart() {
        HeartbeatMessage heartbeatMessage  = new HeartbeatMessage();
        String heart = new Gson().toJson(heartbeatMessage);
        return new TextWebSocketFrame(heart);
    }




    /**
     * 默认确认包
     * @param msgId
     * @return
     */
    private String getDefaultUdpAck(String msgId, String fromId, String toId){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setMsgId(msgId);
        ackMessage.setAckType(MSG_ACK_TYPE);
        return   new Gson().toJson(ackMessage);
    }

    /**
     * 默认确认包
     * @param msgId
     * @return
     */
    private PackProtobuf.Pack getDefaultTcpAck(String msgId,Long serial,String fromId,String toId){
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

    /**
     * 默认确认包
     * @param msgId
     * @return
     */
    private TextWebSocketFrame getDefaultWsAck(String msgId,String fromId,String toId){
        AckMessage ackMessage = new AckMessage();
        ackMessage.setMsgId(msgId);
        ackMessage.setAckType(MSG_ACK_TYPE);
        return   new TextWebSocketFrame(new Gson().toJson(ackMessage));
    }









}
