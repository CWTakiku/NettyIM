package com.takiku.im_lib.defaultImpl;

import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * author:chengwl
 * Description:  默认的消息接收器，只针对用户发送的消息，其他类型的消息不在这处理
 * Date:2020/4/18
 */
public class DefaultMessageReceiveHandler implements MessageHandler<PackProtobuf.Pack> {

    private onMessageArriveListener onMessageArriveListener;
    public DefaultMessageReceiveHandler(onMessageArriveListener onMessageArriveListener){
        this.onMessageArriveListener=onMessageArriveListener;
    }

    @Override
    public boolean isFocusMsg(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.MSG;
    }

    @Override
    public void handleMsg(PackProtobuf.Pack pack) {
        onMessageArriveListener.onMessageArrive(pack);
    }

    public interface onMessageArriveListener{
        void onMessageArrive(PackProtobuf.Pack pack);
   }


}
