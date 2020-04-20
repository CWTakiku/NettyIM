package com.takiku.nettyim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.DefaultMessageReceiveHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.clientdemo.IMClientDemo;
import com.takiku.nettyim.clientdemo.IMClientDemo2;

import java.io.IOException;
import java.util.UUID;

import static com.takiku.nettyim.Constants.MSG_REPLY_TYPE;
import static com.takiku.nettyim.Constants.MSG_STATUS_FAILED;
import static com.takiku.nettyim.Constants.MSG_STATUS_READ;
import static com.takiku.nettyim.Constants.MSG_STATUS_SENDING;

public class MainActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private EditText editText1;
    private EditText editText2;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    private Handler mDeliveryHandler;
    private MessageAdapter messageAdapter1;
    private MessageAdapter messageAdapter2;


    public static final String client1UserId="user id1";
    public static final String client2UserId="user id2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAdapter();
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());
        IMClientDemo.getInstance(new DefaultMessageReceiveHandler.onMessageArriveListener() {
            @Override
            public void onMessageArrive(final PackProtobuf.Pack pack) {
                final AppMessage appMessage=AppMessage.buildAppMessage(pack.getMsg());
                IMClientDemo.getInstance().sendReply(getReplyRequest(appMessage.getHead().getMsgId(),appMessage.getHead().getFromId())); //如果有消息到达，马上告诉服务端消息已读 ，此消息应该设置为无需回应
                mDeliveryHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        addClient1Message(appMessage); //UI 显示

                    }
                });
            }
        }).startConnect();

        IMClientDemo2.getInstance(new DefaultMessageReceiveHandler.onMessageArriveListener() {
            @Override
            public void onMessageArrive(final PackProtobuf.Pack pack) {
                final AppMessage appMessage=AppMessage.buildAppMessage(pack.getMsg());
                IMClientDemo2.getInstance().sendReply(getReplyRequest(appMessage.getHead().getMsgId(),appMessage.getHead().getFromId())); //如果有消息到达，马上告诉服务端消息已读，此消息应该设置为无需回应
                mDeliveryHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addClient2Message(appMessage); //UI 显示
                    }
                });
            }
        }).startConnect();


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppMessage appMessage=createAppMessage(client1UserId, client2UserId, editText1.getText().toString());
                editText1.setText("");
                appMessage.msgStatus=MSG_STATUS_SENDING;
                addClient1Message(appMessage);
                IMClientDemo.getInstance().sendMsg(createRequest(appMessage), new Callback() { //发送消息
                    @Override
                    public void onFailure(Call call, IOException e) { //发送失败更新UI
                            appMessage.msgStatus=MSG_STATUS_FAILED;
                            messageAdapter1.onItemChange(appMessage);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.code == Response.SUCCESS) { //收到服务端响应,即代表消息发送成功，更新UI
                            PackProtobuf.Pack pack= (PackProtobuf.Pack) response.body;
                            appMessage.msgStatus=pack.getReply().getStatusReport();
                            System.out.println("msgStatus "+appMessage.msgStatus);
                            mDeliveryHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    messageAdapter1.onItemChange(appMessage);
                                }
                            });
                        }
                    }
                });
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppMessage appMessage=createAppMessage(client2UserId, client1UserId, editText2.getText().toString());
                editText2.setText("");
                appMessage.msgStatus=MSG_STATUS_SENDING;
                addClient2Message(appMessage);
                IMClientDemo.getInstance().sendMsg(createRequest(appMessage), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        appMessage.msgStatus=MSG_STATUS_FAILED;
                        mDeliveryHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                messageAdapter2.onItemChange(appMessage);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.code == Response.SUCCESS) { //收到服务端响应,即代表消息发送成功
                            PackProtobuf.Pack pack= (PackProtobuf.Pack) response.body;
                            appMessage.msgStatus=pack.getReply().getStatusReport();
                            mDeliveryHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    messageAdapter2.onItemChange(appMessage);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void addClient1Message(AppMessage appMessage){
         messageAdapter1.addMessage(appMessage);
    }
    private void addClient2Message(AppMessage appMessage){
        messageAdapter2.addMessage(appMessage);
    }

    private void initAdapter() {
        messageAdapter1=new MessageAdapter(client1UserId);
        messageAdapter2=new MessageAdapter(client2UserId);
        recyclerView1.setAdapter(messageAdapter1);
        recyclerView2.setAdapter(messageAdapter2);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
    }

  private AppMessage createAppMessage(String fromId,String toId,String content){
      AppMessage appMessage=new AppMessage.Builder()
              .setMsgId(UUID.randomUUID().toString())
              .setFromId(fromId)
              .setToId(toId)
              .setBody(content)
              .build();
      return appMessage;
  }

    private Request createRequest(AppMessage appMessage){


        Request request=new Request.Builder().
                setRequestTag(appMessage.getHead().getMsgId()).
                setBody(getMsgPack(appMessage.buildProto())).
                build();
        return request;
    }

    private void initView() {
        btn1=findViewById(R.id.btn_1);
        btn2=findViewById(R.id.btn_2);
        editText1=findViewById(R.id.et_content1);
        editText2=findViewById(R.id.et_content2);
        recyclerView1=findViewById(R.id.recycler1);
        recyclerView2=findViewById(R.id.recycler2);
    }

    public Request getReplyRequest(String msgId,String userId){
        ReplyMessage replyMessage=new ReplyMessage();
        replyMessage.setMsgId(msgId);
        replyMessage.setUserId(userId);
        replyMessage.setReplyType(MSG_REPLY_TYPE);
        replyMessage.setStatusReport(MSG_STATUS_READ);
        Request replyRequest=  new Request.Builder()
                .setNeedResponse(false) //设置为不需要应答
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
