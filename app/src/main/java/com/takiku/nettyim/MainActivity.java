package com.takiku.nettyim;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.handler.DefaultShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShakeHandsMessage shakeHandsMessage =new ShakeHandsMessage();
        shakeHandsMessage.setToken("your token");
        shakeHandsMessage.setUserId("your userId");
        shakeHandsMessage.setMsgId("12345678");

        AppMessage appMessage=new AppMessage.Builder().build();

        PackProtobuf.ShakeHands shakeHands=shakeHandsMessage.build();
       PackProtobuf.Pack pack=  PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                .setShakeHands(shakeHands)
                .build();
       PackProtobuf.Heart heart=PackProtobuf.Heart.newBuilder().setUserId("your userId").build();
       PackProtobuf.Pack heartPack=PackProtobuf.Pack.newBuilder().setPackType(PackProtobuf.Pack.PackType.HEART).setHeart(heart).build();

       IMClient imClient=new IMClient.Builder()
               .setShakeHands(pack, new DefaultShakeHandsHandler())
               .setHeartBeatMsg(heartPack)
               .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
               .build();

//        Request request=new Request.Builder().setBody(appMessage).build();
//        imClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//        });

//        Request request=new Request.Builder()
//                .setAddress(new Address("192.168.69.32",8765,Address.Type.SOCKS))
//                .setBody(shakeHandsMessage)
//                .build();
//       Call call= imClient.newCall(request);
//       call.enqueue(new Callback() {
//           @Override
//           public void onFailure(Call call, IOException e) {
//
//           }
//
//           @Override
//           public void onResponse(Call call, Response response) throws IOException {
//
//           }
//       });
    }
}
