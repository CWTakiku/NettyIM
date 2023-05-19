package com.takiku.nettyim.udpClientDemo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.nettyim.Constants;


import java.nio.charset.StandardCharsets;

import io.netty.channel.socket.DatagramPacket;

import static com.takiku.im_lib.defaultImpl.DefaultMessageShakeHandsHandler.SHAKE_HANDS_ACK_TYPE;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/18
 */
class UdpMessageShakeHandsHandler implements MessageShakeHandsHandler {

    private DatagramPacket datagramPacket;

    public UdpMessageShakeHandsHandler(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    @Override
    public Object ShakeHands() {
        return datagramPacket;
    }

    @Override
    public boolean isShakeHands(Object msg) {
        if (msg instanceof DatagramPacket) {
            String data = ((DatagramPacket) msg).content().toString(StandardCharsets.UTF_8);
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(data);
            if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE) {
                AckMessage ackMessage = new Gson().fromJson(data, AckMessage.class);

                if (ackMessage.getAckType() == Constants.SHAKE_HANDS_ACK_TYPE) {
                    return true;
                }

                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isShakeHandsOk(Object pack) {
        if (pack instanceof DatagramPacket) {
            String data = ((DatagramPacket) pack).content().toString(StandardCharsets.UTF_8);
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(data);
            if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE){
                AckMessage ackMessage = new Gson().fromJson(data, AckMessage.class);
                if (ackMessage.getAckType() == SHAKE_HANDS_ACK_TYPE) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
