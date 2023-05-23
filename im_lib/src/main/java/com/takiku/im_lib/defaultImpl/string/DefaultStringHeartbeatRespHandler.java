package com.takiku.im_lib.defaultImpl.string;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.util.LogUtil;


import java.nio.charset.StandardCharsets;

import io.netty.channel.socket.DatagramPacket;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class DefaultStringHeartbeatRespHandler implements MessageHandler<String> {
    public static final int HEART_ACK_TYPE=0x11;//与服务端保持类型统一
    @Override
    public boolean isFocusMsg(Object msg) {

        JsonObject jsonObject  =(JsonObject) new JsonParser().parse((String) msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage ackMessage = new Gson().fromJson((String)msg,AckMessage.class);
            return ackMessage.getAckType()==HEART_ACK_TYPE;
        }
        return false;
    }

    @Override
    public void handleMsg(String textWebSocketFrame) {
        LogUtil.i("WsHeartbeatRespHandler","收到心跳包的响应");
    }
}
