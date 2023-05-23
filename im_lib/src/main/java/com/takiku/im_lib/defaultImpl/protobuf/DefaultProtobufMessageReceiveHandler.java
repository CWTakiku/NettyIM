package com.takiku.im_lib.defaultImpl.protobuf;

import com.takiku.im_lib.defaultImpl.callback.OnMessageArriveListener;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * author:chengwl
 * Description:  默认的消息接收器，只针对用户发送的消息，其他类型的消息不在这处理
 * Date:2020/4/18
 */
public class DefaultProtobufMessageReceiveHandler implements MessageHandler<PackProtobuf.Pack> {

    private OnMessageArriveListener onMessageArriveListener;
    public DefaultProtobufMessageReceiveHandler(OnMessageArriveListener onMessageArriveListener){
        this.onMessageArriveListener=onMessageArriveListener;
    }

    @Override
    public boolean isFocusMsg(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.MSG;
    }

    @Override
    public void handleMsg(PackProtobuf.Pack pack) {
        AppMessage appMessage=AppMessage.buildAppMessage(pack.getMsg());
        onMessageArriveListener.onMessageArrive(appMessage);
    }




}
