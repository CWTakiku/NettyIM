package com.takiku.nettyim.udp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.nettyim.util.Constants;


import static com.takiku.im_lib.defaultImpl.DefaultMessageShakeHandsHandler.SHAKE_HANDS_ACK_TYPE;

/**
 * author:chengwl
 * Description:
 * Date:2023/5/18
 */
public class UdpMessageShakeHandsHandler implements MessageShakeHandsHandler<String,String> {

    private String shake;

    public UdpMessageShakeHandsHandler(String shake) {
        this.shake = shake;
    }

    @Override
    public String ShakeHands() {
        return shake;
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
    public boolean isShakeHandsOk(String msg) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage ackMessage = new Gson().fromJson(msg, AckMessage.class);
            if (ackMessage.getAckType() == SHAKE_HANDS_ACK_TYPE) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
