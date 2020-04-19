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
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
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


                CommonIMClient.getInstance().sendMsg(request, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                         if (response!=null&&response.code==Response.SUCCESS){
                             CommonIMClient.getInstance().sendReply(getReplyRequest());
                         }
                    }
                });

            }
        });

    }

    public Request getReplyRequest(){
        ReplyMessage replyMessage=new ReplyMessage();
        replyMessage.setMsgId("11111");
        replyMessage.setStatusReport(1);
        Request replyRequest=  new Request.Builder()
                .setNeedResponse(false)
                .setBody(getReplyPack(replyMessage.buildProto()))
                .build();
        return replyRequest;
    }
    public PackProtobuf.Pack getMsgPack(PackProtobuf.Msg  msg){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.MSG)
                .setMsg(msg)
                .build();
    }
    public PackProtobuf.Pack getReplyPack(PackProtobuf.Reply  reply){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply(reply)
                .build();
    }


}
