package com.takiku.nettyim.udpClientDemo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.util.LogUtil;


import java.nio.charset.StandardCharsets;

import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class UdpMessageReceiveHandler implements MessageHandler<DatagramPacket> {
    private onMessageArriveListener listener;
    public UdpMessageReceiveHandler(onMessageArriveListener onMessageArriveListener){
        this.listener = onMessageArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        String data =((DatagramPacket)msg).content().toString(StandardCharsets.UTF_8);
        JsonObject jsonObject  =(JsonObject) new JsonParser().parse(data);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_MSG_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(DatagramPacket datagramPacket) {
      //  LogUtil.i("WSMessageReceiveHandler","type "+textWebSocketFrame.text());
        if (listener!=null){
         //   LogUtil.i("WSMessageReceiveHandler","type --- "+textWebSocketFrame.text());
            listener.onMessageArrive(datagramPacket);
        }
    }
    public interface onMessageArriveListener{
        void onMessageArrive(DatagramPacket pack);
    }

}
