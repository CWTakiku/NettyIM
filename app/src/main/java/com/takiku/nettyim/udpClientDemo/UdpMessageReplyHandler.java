package com.takiku.nettyim.udpClientDemo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;




/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class UdpMessageReplyHandler implements MessageHandler<String> {
    public UdpMessageReplyHandler(OnReplyArriveListener onReplyArriveListener){
        this.listener = onReplyArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        JsonObject jsonObject  =(JsonObject) new JsonParser().parse((String) msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_REPLY_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(String datagramPacket) {
          ReplyMessage replyMessage = new Gson().fromJson(datagramPacket,ReplyMessage.class);
          listener.onReplyArrive(replyMessage);

    }
    public interface OnReplyArriveListener{
        void onReplyArrive(ReplyMessage pack);
    }
    private OnReplyArriveListener listener;
}
