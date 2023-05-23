package com.takiku.nettyim.udpClientDemo;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.nettyim.Constants;

import java.nio.charset.StandardCharsets;

import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.takiku.nettyim.Constants.MSG_ACK_TYPE;

/**
 * @author chengwl
 * @des udp的消息确认
 * @Date:2023/5/16
 */
public class UdpAckConsumer implements Consumer<String> {

    @Override
    public boolean Observable(String data, String requestTag) {

        JsonObject jsonObject  =(JsonObject) new JsonParser().parse(data);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage ackMessage = new Gson().fromJson(data,AckMessage.class);
            System.out.println("asd "+data);
            return ackMessage.getMsgId().equals(requestTag)&&ackMessage.getAckType()== MSG_ACK_TYPE;
        }
        return false;
    }

    @Override
    public void accept(String datagramPacket) {

    }
}
