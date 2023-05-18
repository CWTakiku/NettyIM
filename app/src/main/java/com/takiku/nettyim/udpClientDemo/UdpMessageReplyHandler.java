package com.takiku.nettyim.udpClientDemo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;


import java.nio.charset.StandardCharsets;

import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class UdpMessageReplyHandler implements MessageHandler<DatagramPacket> {
    public UdpMessageReplyHandler(OnReplyArriveListener onReplyArriveListener){
        this.listener = onReplyArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        String data =((DatagramPacket)msg).content().toString(StandardCharsets.UTF_8);
        JsonObject jsonObject  =(JsonObject) new JsonParser().parse(data);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_REPLY_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(DatagramPacket datagramPacket) {
          ReplyMessage replyMessage = new Gson().fromJson(datagramPacket.content().toString(StandardCharsets.UTF_8),ReplyMessage.class);
          listener.onReplyArrive(replyMessage);

    }
    public interface OnReplyArriveListener{
        void onReplyArrive(ReplyMessage pack);
    }
    private OnReplyArriveListener listener;
}
