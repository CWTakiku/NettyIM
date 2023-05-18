package com.takiku.nettyim.wsClientDemo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.util.LogUtil;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2023/1/31
 */
public class WsHeartbeatRespHandler implements MessageHandler<TextWebSocketFrame> {
    public static final int HEART_ACK_TYPE=0x11;//与服务端保持类型统一
    @Override
    public boolean isFocusMsg(Object msg) {
        if (msg instanceof TextWebSocketFrame){
            JsonObject jsonObject =(JsonObject) new JsonParser().parse(((TextWebSocketFrame)msg).text());
            JsonElement type =   jsonObject.get("packType");
            if (type.getAsInt() == Request.PACK_ACK_TYPE){
                AckMessage appMessage = new Gson().fromJson(((TextWebSocketFrame)msg).text(),AckMessage.class);
                return appMessage.getAckType()==HEART_ACK_TYPE;
            }
        }
        return false;
    }

    @Override
    public void handleMsg(TextWebSocketFrame textWebSocketFrame) {
        LogUtil.i("WsHeartbeatRespHandler","收到心跳包的响应");
    }
}
