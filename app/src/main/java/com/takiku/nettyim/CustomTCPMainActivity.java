package com.takiku.nettyim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.customTcpClientdemo.IMClientDemo;
import com.takiku.nettyim.customTcpClientdemo.IMClientDemo2;
import com.takiku.nettyim.widget.MenuItemPopWindow;
import com.takiku.nettyim.widget.MessageAdapter;

import java.io.IOException;
import java.util.UUID;

import static com.takiku.nettyim.Constants.MSG_REPLY_TYPE;
import static com.takiku.nettyim.Constants.MSG_STATUS_FAILED;
import static com.takiku.nettyim.Constants.MSG_STATUS_READ;
import static com.takiku.nettyim.Constants.MSG_STATUS_SEND;
import static com.takiku.nettyim.Constants.MSG_STATUS_SENDING;
import static com.takiku.nettyim.Constants.MSG_STATUS_WITHDRAW;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * create by cwl
 * 这里一个页面直接模拟两个客户端通信
 */
public class CustomTCPMainActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private Button btnLine1;
    private Button btnLine2;
    private EditText editText1;
    private EditText editText2;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    private Handler mDeliveryHandler;
    private MessageAdapter messageAdapter1;
    private MessageAdapter messageAdapter2;


    public static final String client1UserId="userid1";
    public static final String client2UserId="userid2";
    private boolean client1Online=true;
    private boolean client2Online=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAdapter();
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());
        IMClientDemo.getInstance().startConnect();
        IMClientDemo.getInstance().registerMessageReceive(appMessage->{addClient1Message(appMessage);});
        IMClientDemo.getInstance().registerReplyReceive(replyMessage ->{updateClient1MessageStatus(replyMessage);});
        IMClientDemo2.getInstance().startConnect();
        IMClientDemo2.getInstance().registerMessageReceive(appMessage->{addClient2Message(appMessage);});
        IMClientDemo2.getInstance().registerReplyReceive(replyMessage ->{updateClient2MessageStatus(replyMessage);});

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppMessage appMessage=createAppMessage(client1UserId, client2UserId, editText1.getText().toString());
                editText1.setText("");
                appMessage.msgStatus=MSG_STATUS_SENDING;
                addClient1Message(appMessage);
                IMClientDemo.getInstance().sendMsgUICallback(createRequest(appMessage,1), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        appMessage.msgStatus = MSG_STATUS_FAILED;
                        messageAdapter1.onItemChange(appMessage); //更新一条消息状态
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        appMessage.msgStatus=MSG_STATUS_SEND;
                        messageAdapter1.onItemChange(appMessage); //更新一条消息状态
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
                IMClientDemo2.getInstance().sendMsgUICallback(createRequest(appMessage,2), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        appMessage.msgStatus = MSG_STATUS_FAILED; //发送失败，指规定时间内服务器无应答且进行了发送重试依然没有响应
                        messageAdapter2.onItemChange(appMessage);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        appMessage.msgStatus=MSG_STATUS_SEND;
                        messageAdapter2.onItemChange(appMessage); //更新一条消息状态
                    }
                });
            }
        });

        btnLine1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client1Online=!client1Online;
                if (client1Online){
                    btnLine1.setText("下线");
                    IMClientDemo.getInstance().startConnect();
                }else {
                    btnLine1.setText("上线");
                    IMClientDemo.getInstance().disConnect();
                }
            }
        });
        btnLine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client2Online=!client2Online;
                if (client2Online){
                    btnLine2.setText("下线");
                    IMClientDemo2.getInstance().startConnect();
                }else {
                    btnLine2.setText("上线");
                    IMClientDemo2.getInstance().disConnect();
                }
            }
        });
    }

    //UI显示一条发送消息
    private void addClient1Message(AppMessage appMessage){
         messageAdapter1.addMessage(appMessage);
         recyclerView1.scrollToPosition(messageAdapter1.getItemCount()-1);


    }
    private void addClient2Message(AppMessage appMessage){
        messageAdapter2.addMessage(appMessage);
        recyclerView2.scrollToPosition(messageAdapter2.getItemCount()-1);

    }
    public void updateClient1MessageStatus(ReplyMessage replyMessage){
        Log.i("TAG", "updateClient1MessageStatus: "+replyMessage.getReplyType());
        messageAdapter1.updateMessage(replyMessage);
    }
    public void updateClient2MessageStatus(ReplyMessage replyMessage){
        messageAdapter2.updateMessage(replyMessage);
    }

    private void initAdapter() {
        messageAdapter1=new MessageAdapter(client1UserId);
        messageAdapter2=new MessageAdapter(client2UserId);
        messageAdapter1.setOperationMessageListener(((appMessage, flag) -> {
            int status=-1;
            switch (flag){
                case MenuItemPopWindow.MENU_TYPE_READ:
                    status=MSG_STATUS_READ;
                    break;
                    case MenuItemPopWindow.MENU_TYPE_RECALL:
                     status=MSG_STATUS_WITHDRAW;
                     break;

            }
            if (status!=-1){
                IMClientDemo.getInstance().sendMsg(createReplyRequest(appMessage.getHead().getMsgId(),client1UserId, client2UserId, status,1), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //消息发送失败
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        //消息发送成功
                    }
                });
            }
        }));
        messageAdapter2.setOperationMessageListener(((appMessage, flag) -> {
            int status=-1;
            switch (flag){
                case MenuItemPopWindow.MENU_TYPE_READ:
                    status=MSG_STATUS_READ;
                    break;
                case MenuItemPopWindow.MENU_TYPE_RECALL:
                    status=MSG_STATUS_WITHDRAW;
                    break;
            }
            if (status!=-1){
                IMClientDemo2.getInstance().sendMsg(createReplyRequest(appMessage.getHead().getMsgId(),client2UserId, client1UserId, status,2), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //消息发送失败
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        //消息发送成功
                    }
                });
            }
        }));
        recyclerView1.setAdapter(messageAdapter1);
        recyclerView2.setAdapter(messageAdapter2);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setStackFromEnd(true);
        linearLayoutManager1.scrollToPositionWithOffset(messageAdapter1.getItemCount() - 1, Integer.MIN_VALUE);

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setStackFromEnd(true);
        linearLayoutManager2.scrollToPositionWithOffset(messageAdapter2.getItemCount() - 1, Integer.MIN_VALUE);
        recyclerView1.setLayoutManager(linearLayoutManager1);
        recyclerView2.setLayoutManager(linearLayoutManager2);
    }


    /**
     * 创建应用层消息
     * @param fromId
     * @param toId
     * @param content
     * @return
     */
  private AppMessage createAppMessage(String fromId,String toId,String content){
      AppMessage appMessage=new AppMessage.Builder()
              .setMsgId(UUID.randomUUID().toString())
              .setFromId(fromId)
              .setToId(toId)
              .setBody(content)
              .build();
      return appMessage;
  }

    /**
     * 创建一个消息发送请求
     * @param appMessage
     * @return
     */
    private Request createRequest(AppMessage appMessage,int clientNum){
        String json = new Gson().toJson(appMessage);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(json);
        Request request=new Request.Builder().
                setNeedACK(appMessage.getHead().getMsgId())
                .setBody(getMsgPack(appMessage.buildProto(clientNum == 1?IMClientDemo.getInstance().getMsgSerialID():IMClientDemo2.getInstance().getMsgSerialID())))
                .setSendRetry(false)
                .build();
        return request;
    }

    private void initView() {
        btn1=findViewById(R.id.btn_1);
        btn2=findViewById(R.id.btn_2);
        editText1=findViewById(R.id.et_content1);
        editText2=findViewById(R.id.et_content2);
        recyclerView1=findViewById(R.id.recycler1);
        recyclerView2=findViewById(R.id.recycler2);
        btnLine1=findViewById(R.id.btn_line1);
        btnLine2=findViewById(R.id.btn_line2);
    }

    /**
     * 构建消息状态回复请求
     * @param msgId
     * @return
     */
    public Request createReplyRequest(String msgId,String fromId,String toId,int status,int clientNum){
        ReplyMessage replyMessage=new ReplyMessage();
        replyMessage.setMsgId(msgId);
        replyMessage.setToId(toId);
        replyMessage.setFromId(fromId);
        replyMessage.setReplyType(MSG_REPLY_TYPE);
        replyMessage.setStatusReport(status); //已读
        Request replyRequest=  new Request.Builder()
                .setNoNeedACK() //设置为不需要应答
                .setBody(getReplyPack(replyMessage.buildProto(clientNum == 1?IMClientDemo.getInstance().getMsgSerialID():IMClientDemo2.getInstance().getMsgSerialID())))
                .build();
        return replyRequest;
    }

    /**
     * 构建消息包
     * @param msg
     * @return
     */
    public PackProtobuf.Pack getMsgPack(PackProtobuf.Msg  msg){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.MSG)
                .setMsg(msg)
                .build();
    }

    /**
     * 构建回复包
     * @param reply
     * @return
     */
    public PackProtobuf.Pack getReplyPack(PackProtobuf.Reply  reply){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply(reply)
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IMClientDemo.getInstance().unregisterMessageReceive();
        IMClientDemo.getInstance().unregisterReplyReceive();

        IMClientDemo2.getInstance().unregisterMessageReceive();
        IMClientDemo2.getInstance().unregisterReplyReceive();

    }
}
