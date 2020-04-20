package com.takiku.im_lib.internal;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.internal.handler.MessageReceiveHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/18
 */
public class DefaultMessageReceiveHandler implements MessageReceiveHandler<PackProtobuf.Pack> {

    private onMessageArriveListener onMessageArriveListener;
    public DefaultMessageReceiveHandler(onMessageArriveListener onMessageArriveListener){
        this.onMessageArriveListener=onMessageArriveListener;
    }

    @Override
    public boolean isClientMessage(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.MSG;
    }

    @Override
    public void receiveMessage(PackProtobuf.Pack pack) {
        //System.out.println("收到其他客户端发来的消息："+pack.toString());
        onMessageArriveListener.onMessageArrive(pack);
    }

   public interface onMessageArriveListener{
        void onMessageArrive(PackProtobuf.Pack pack);
   }


}
