package com.takiku.nettyim;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.codec.DefaultCodec;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.ConnectRequest;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.DefaultMessageReceiveHandler;
import com.takiku.im_lib.internal.DefaultMessageRespHandler;
import com.takiku.im_lib.internal.DefaultShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/18
 */
public class CommonIMClient {

    private static CommonIMClient instance;
    private IMClient imClient;
    private CommonIMClient(){
        imClient=new IMClient.Builder()
                .setCodec(new DefaultCodec()) //默认的编解码，开发者可以使用自己的protobuf编解码
                .setShakeHands(getDefaultHands(),new DefaultShakeHandsHandler()) //设置握手认证，可选
                .setHeartBeatMsg(getDefaultHeart()) //设置心跳,可选
                .setMessageRespHandler(new DefaultMessageRespHandler()) //消息响应，开发者可自行定制实现MessageRespHandler接口即可
                .setMessageReceiveHandler(new DefaultMessageReceiveHandler())
                .setAddress(new Address("192.168.8.154",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
    }
    public static CommonIMClient getInstance(){
        if (instance==null){
            synchronized (CommonIMClient.class){
                if (instance==null){
                    instance=new CommonIMClient();
                }
            }
            return instance;
        }
        return instance;
    }

    public void startConnect(){
        imClient.startConnect();
    }

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
                .setHeart(PackProtobuf.Heart.newBuilder().setUserId("your userId").build())
                .build();
    }

    /**
     * 构建握手proto，开发者可自行定制自己的握手包，但需实现ShakeHandsHandler接口
     * @return
     */
    private GeneratedMessageV3 getDefaultHands() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("your token");
        shakeHandsMessage.setUserId("your userId");
        shakeHandsMessage.setMsgId("12345678");
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHandsMessage.buildProto())
                .build();
    }

}
