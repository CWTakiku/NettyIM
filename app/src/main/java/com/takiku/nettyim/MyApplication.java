package com.takiku.nettyim;

import android.app.Application;
import android.content.Context;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.internal.DefaultMessageRespHandler;
import com.takiku.im_lib.internal.DefaultShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class MyApplication extends Application {

    public static IMClient imClient;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initIMClient();
    }

    private void initIMClient() {
        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("your token");
        shakeHandsMessage.setUserId("your userId");
        shakeHandsMessage.setMsgId("12345678");
        PackProtobuf.Heart heart=PackProtobuf.Heart.newBuilder().setUserId("your userId").build();

        imClient=new IMClient.Builder()
                .setShakeHands(getShakeHandsPack(shakeHandsMessage.buildProto()), new DefaultShakeHandsHandler())
                .setHeartBeatMsg(getHeartBeatPack(heart))
                .setMessageRespHandler(new DefaultMessageRespHandler())
                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
                .setAddress(new Address("www.baidu.com",8765,Address.Type.HTTP))
                .build();
        imClient.startConnect();
    }
    public PackProtobuf.Pack getShakeHandsPack(PackProtobuf.ShakeHands  shakeHands){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHands)
                .build();
    }
    public PackProtobuf.Pack getHeartBeatPack(PackProtobuf.Heart  heart){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.HEART)
                .setHeart(heart)
                .build();
    }
}
