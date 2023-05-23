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



import static com.takiku.im_lib.defaultImpl.DefaultMessageShakeHandsHandler.SHAKE_HANDS_ACK_TYPE;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/18
 */
class UdpMessageShakeHandsHandler implements MessageShakeHandsHandler {

    private String datagramPacket;

    public UdpMessageShakeHandsHandler(String datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    @Override
    public Object ShakeHands() {
        return datagramPacket;
    }

    @Override
    public boolean isShakeHands(Object msg) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse((String) msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE) {
            AckMessage ackMessage = new Gson().fromJson((String)msg, AckMessage.class);

            if (ackMessage.getAckType() == Constants.SHAKE_HANDS_ACK_TYPE) {
                return true;
            }

            return false;
        }
        return false;
    }

    @Override
    public boolean isShakeHandsOk(Object pack) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse((String) pack);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage ackMessage = new Gson().fromJson((String)pack, AckMessage.class);
            if (ackMessage.getAckType() == SHAKE_HANDS_ACK_TYPE) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
