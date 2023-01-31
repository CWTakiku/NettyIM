package com.takiku.nettyim.wsClientdemo;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.base.Request;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class WSAckConsumer implements Consumer<TextWebSocketFrame> {
    @Override
    public boolean Observable(TextWebSocketFrame textWebSocketFrame, String requestTag) {


        JsonObject jsonObject =(JsonObject) new JsonParser().parse(textWebSocketFrame.text());
        JsonElement type =   jsonObject.get("packType");
        Log.i("WSAckConsumer","type "+type.getAsInt());
        if (type.getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage appMessage = new Gson().fromJson(textWebSocketFrame.text(),AckMessage.class);
            return appMessage.getMsgId().equals(requestTag);
        }
        return false;
    }

    @Override
    public void accept(TextWebSocketFrame textWebSocketFrame) {

    }
}
