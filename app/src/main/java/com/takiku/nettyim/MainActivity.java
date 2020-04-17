package com.takiku.nettyim;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Address;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.DefaultMessageRespHandler;
import com.takiku.im_lib.internal.DefaultShakeHandsHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private Handler mDeliveryHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.btn_send);
        textView=findViewById(R.id.tv_response);
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppMessage appMessage=new AppMessage.Builder()
                        .setMsgId("11111")
                        .setFromId("your userId")
                        .setBody("我是真实内容!")
                        .build();


                Request request=new Request.Builder().
                        setRequestTag(appMessage.getHead().getMsgId()).
                        setBody(getMsgPack(appMessage.buildProto())).
                        build();
                MyApplication.imClient.newCall(request).enqueue(new Callback<PackProtobuf.Pack>() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, final Response<PackProtobuf.Pack> response) throws IOException {
                        if (response!=null&&response.code==Response.SUCCESS){
                            mDeliveryHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                 textView.setText(response.body.getReply().toString());
                                }
                            });
                        }
                    }
                });
            }
        });

    }
    public PackProtobuf.Pack getMsgPack(PackProtobuf.Msg  msg){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.MSG)
                .setMsg(msg)
                .build();
    }


}
