package com.takiku.nettyim;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.listener.EventListener;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.im_lib.protocol.IMProtocol;
import com.takiku.nettyim.widget.MenuItemPopWindow;
import com.takiku.nettyim.widget.MessageAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.takiku.im_lib.util.Constants.MSG_REPLY_TYPE;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_FAILED;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_READ;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_SEND;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_SENDING;
import static com.takiku.im_lib.util.Constants.MSG_STATUS_WITHDRAW;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/22
 */
public class MainActivity extends AppCompatActivity {

    private Client demo1 ,demo2;


    private String localHost= "192.168.31.212"; //更改为你电脑的ip地址


    private Button btnSend1;
    private Button btnSend2;
    private MessageAdapter messageAdapter1;
    private MessageAdapter messageAdapter2;
    private EditText editText1;
    private EditText editText2;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    public static final String client1UserId="userid1";
    public static final String client2UserId="userid2";
    private TextView tvStatus1,tvStatus2;
    private static final String TAG = "MainActivity";

    private int protocol;
    private int codecType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        protocol =   getIntent().getIntExtra("protocol",IMProtocol.PRIVATE);
        codecType = getIntent().getIntExtra("codecType",0);
        if (protocol == IMProtocol.WEB_SOCKET){
           localHost = "ws://"+localHost+":8804/ws";//或者wss地址
        }
        Log.i("MainActivity","protocol:"+protocol+" ip:"+localHost);
        initView();
        initAdapter();
        initDemo();


    }





    private void initDemo(){
        demo1 = new Client(localHost,client1UserId,protocol,codecType,new MyEventListener(client1UserId,tvStatus1));
        demo2 = new Client(localHost,client2UserId, protocol,codecType,new MyEventListener(client2UserId,tvStatus2));
        demo1.registerMessageReceive(appMessage->{addClient1Message(appMessage);});
        demo1.registerReplyReceive(replyMessage -> {updateClient1MessageStatus(replyMessage);});

        demo2.registerMessageReceive(appMessage->{addClient2Message(appMessage);});
        demo2.registerReplyReceive(replyMessage -> {updateClient2MessageStatus(replyMessage);});

        btnSend1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppMessage appMessage=createAppMessage(client1UserId, client2UserId, editText1.getText().toString());
                editText1.setText("");
                appMessage.msgStatus=MSG_STATUS_SENDING;
                addClient1Message(appMessage);
               demo1.sendMsgUICallback(createRequest(appMessage,1), new Callback() {
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
        btnSend2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppMessage appMessage=createAppMessage(client2UserId, client1UserId, editText2.getText().toString());
                editText2.setText("");
                appMessage.msgStatus=MSG_STATUS_SENDING;
                addClient2Message(appMessage);
               demo2.sendMsgUICallback(createRequest(appMessage,2), new Callback() {
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
        demo1.startConnect();
        demo2.startConnect();
    }




    private void initView() {
        btnSend1 =findViewById(R.id.btn_1);
        btnSend2 =findViewById(R.id.btn_2);
        editText1=findViewById(R.id.et_content1);
        editText2=findViewById(R.id.et_content2);
        recyclerView1=findViewById(R.id.recycler1);
        recyclerView2=findViewById(R.id.recycler2);
        tvStatus1 = findViewById(R.id.tv_status1);
        tvStatus2 =  findViewById(R.id.tv_status2);
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
              demo1.sendMsg(createReplyRequest(appMessage.getHead().getMsgId(),client1UserId, client2UserId, status,1), new Callback() {
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
             demo2.sendMsg(createReplyRequest(appMessage.getHead().getMsgId(),client2UserId, client1UserId, status,2), new Callback() {
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
     * @param
     * @return
     */
    private Request createRequest(AppMessage appMessage,int clientNum){
        Request.Builder builder = new Request.Builder().
                 setNeedACK(appMessage.getHead().getMsgId())
                .setSendRetry(true);
        if (protocol == IMProtocol.PRIVATE){
            if (codecType == 0){
                builder.setBody(getProtobufMsgPack(appMessage.buildProto(clientNum == 1?demo1.getMsgSerialID():demo2.getMsgSerialID())));
            }else {
                builder.setBody(getStringMsgPack(appMessage));
            }

        }else if (protocol == IMProtocol.WEB_SOCKET){
            builder.setBody(getWsMsgPack(appMessage));
        }else if (protocol == IMProtocol.UDP){
            builder.setBody(getStringMsgPack(appMessage));
        }
        return builder.build();
    }


    /**
     * 构建消息包
     * @param msg
     * @return
     */
    public PackProtobuf.Pack getProtobufMsgPack(PackProtobuf.Msg  msg){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.MSG)
                .setMsg(msg)
                .build();
    }

    /**
     * 构建消息包
     * @param msg
     * @return
     */
    public TextWebSocketFrame getWsMsgPack(AppMessage  msg){
        String json = new Gson().toJson(msg);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(json);
        return textWebSocketFrame;
    }

    /**
     * 构建消息包
     * @param msg
     * @return
     */
    public String getStringMsgPack(AppMessage  msg){
        String json = new Gson().toJson(msg);
        return json;
    }


    /**
     * 构建消息状态回复请求
     * @param msgId
     * @return
     */
    public Request createReplyRequest(String msgId, String fromId, String toId, int status, int clientNum){
        ReplyMessage replyMessage=new ReplyMessage();
        replyMessage.setMsgId(msgId);
        replyMessage.setToId(toId);
        replyMessage.setFromId(fromId);
        replyMessage.setReplyType(MSG_REPLY_TYPE);
        replyMessage.setStatusReport(status); //已读

        Request.Builder builder =  new Request.Builder()
                .setNoNeedACK();//设置为不需要应答
        if (protocol == IMProtocol.PRIVATE){
            if (codecType == 0){
                builder.setBody(getProtobufReplyPack(replyMessage.buildProto(clientNum == 1?demo1.getMsgSerialID():demo2.getMsgSerialID())));
            }else {
                builder.setBody(getStringReplyPack(replyMessage));
            }
        }else if (protocol == IMProtocol.WEB_SOCKET){
            builder.setBody(getWsReplyPack(replyMessage));
        }else if (protocol == IMProtocol.UDP){
            builder.setBody(getStringReplyPack(replyMessage));
        }

        return builder.build();
    }



    /**
     * 构建回复包
     * @param reply
     * @return
     */
    public PackProtobuf.Pack getProtobufReplyPack(PackProtobuf.Reply  reply){
        return PackProtobuf.Pack.newBuilder()
                .setPackType(PackProtobuf.Pack.PackType.REPLY)
                .setReply(reply)
                .build();
    }

    /**
     * 构建回复包
     *
     * @param reply
     * @return
     */
    public String getStringReplyPack(ReplyMessage  reply){
        String json = new Gson().toJson(reply);
        return json;
    }
    /**
     * 构建回复包
     * @param reply
     * @return
     */
    public TextWebSocketFrame getWsReplyPack(ReplyMessage  reply){
        String json = new Gson().toJson(reply);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(json);
        return textWebSocketFrame;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        demo1.unregisterMessageReceive();
        demo1.unregisterReplyReceive();


        demo2.unregisterMessageReceive();
        demo2.unregisterReplyReceive();

        demo1.disConnect();
        demo2.disConnect();

    }

    public class MyEventListener extends EventListener{

        private String userId;
        private TextView textView;
        public MyEventListener(String userId,TextView textView){
            this.textView =textView;
            this.userId = userId;
        }
        @Override
        public void connectStart(InetSocketAddress inetSocketAddress) {
            super.connectStart(inetSocketAddress);
            Log.i(TAG,"connectStart");
            textView.setText("connectStart");
        }

        @Override
        public void connectSuccess() {
            super.connectSuccess();
            Log.i(TAG,"connectSuccess");
            textView.setText("connectSuccess");
        }

        @Override
        public void connectionException(Throwable throwable) {
            super.connectionException(throwable);
        }

        @Override
        public void connectFailed(InetSocketAddress inetSocketAddress, IOException ioe) {
            super.connectFailed(inetSocketAddress, ioe);
            Log.i(TAG,"connectFailed");
            textView.setText("connectFailed");
        }

        @Override
        public void connectionBroken() {
            super.connectionBroken();
            textView.setText("connectionBroken");
            Log.i(TAG,"connectionBroken");
        }

        @Override
        public void connectionReleased(Connection connection) {
            super.connectionReleased(connection);
            Log.i(TAG,"connectionReleased");
            textView.setText("connectionReleased");
        }

        @Override
        public void sendMsgStart(Call call) {
            super.sendMsgStart(call);
            Log.i(TAG,"sendMsgStart");
            textView.setText("sendMsgStart");
        }

        @Override
        public void sendMsgEnd(Call call) {
            super.sendMsgEnd(call);
            Log.i(TAG,"sendMsgEnd");
            textView.setText("sendMsgEnd");
        }

        @Override
        public void sendMsgFailed(Call call) {
            super.sendMsgFailed(call);
            Log.i(TAG,"sendMsgFailed");
            textView.setText("sendMsgFailed");
        }
    }
}
