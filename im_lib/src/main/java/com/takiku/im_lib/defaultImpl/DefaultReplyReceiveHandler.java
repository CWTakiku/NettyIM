package com.takiku.im_lib.defaultImpl;

import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class DefaultReplyReceiveHandler implements MessageHandler<PackProtobuf.Pack> {
    @Override
    public boolean isFocusMsg(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY;
    }
    public DefaultReplyReceiveHandler(OnReplyArriveListener onReplyListener){
        this.listener=onReplyListener;
    }

    @Override
    public void handleMsg(PackProtobuf.Pack pack) {
       listener.onReplyArrive(pack);
    }
    public interface OnReplyArriveListener{
       void onReplyArrive(PackProtobuf.Pack pack);
    }
    private OnReplyArriveListener listener;
}
