package com.takiku.nettyim.tcp;


import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.nettyim.callbcak.OnReplyArriveListener;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/22
 */
public class TcpMessageReplyHandler  implements MessageHandler<PackProtobuf.Pack> {

    private OnReplyArriveListener listener;
    @Override
    public boolean isFocusMsg(Object msg) {
        PackProtobuf.Pack pack= (PackProtobuf.Pack) msg;
        return pack.getPackType()==PackProtobuf.Pack.PackType.REPLY;
    }
    public TcpMessageReplyHandler(OnReplyArriveListener onReplyListener){
        this.listener=onReplyListener;
    }

    @Override
    public void handleMsg(PackProtobuf.Pack pack) {
        ReplyMessage replyMessage=ReplyMessage.buildReplyMessage(pack.getReply());
        listener.onReplyArrive(replyMessage);
    }

}
