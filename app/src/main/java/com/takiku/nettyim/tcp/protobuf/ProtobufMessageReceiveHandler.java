package com.takiku.nettyim.tcp.protobuf;

import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.callbcak.OnMessageArriveListener;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/22
 */
public class ProtobufMessageReceiveHandler implements MessageHandler<PackProtobuf.Pack> {

    private OnMessageArriveListener listener;
    public ProtobufMessageReceiveHandler(OnMessageArriveListener listener){
        this.listener = listener;
    }

    @Override
    public boolean isFocusMsg(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.MSG;
    }

    @Override
    public void handleMsg(PackProtobuf.Pack pack) {
        AppMessage appMessage=AppMessage.buildAppMessage(pack.getMsg());
        listener.onMessageArrive(appMessage);
    }

}
