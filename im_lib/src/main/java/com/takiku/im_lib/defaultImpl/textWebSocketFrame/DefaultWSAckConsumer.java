package com.takiku.im_lib.defaultImpl.textWebSocketFrame;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.AckMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.nettyim.util.Constants;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des ws的消息确认
 * @date:2022/11/17
 */
public class DefaultWSAckConsumer implements Consumer<TextWebSocketFrame> {

    @Override
    public boolean Observable(TextWebSocketFrame textWebSocketFrame, String requestTag) {


        JsonObject jsonObject =(JsonObject) new JsonParser().parse(textWebSocketFrame.text());
        JsonElement type =   jsonObject.get("packType");
        Log.i("WSAckConsumer","type "+type.getAsInt());
        if (type.getAsInt() == Request.PACK_ACK_TYPE){
            AckMessage ackMessage = new Gson().fromJson(textWebSocketFrame.text(),AckMessage.class);
            return ackMessage.getMsgId().equals(requestTag)&&ackMessage.getAckType()== Constants.MSG_ACK_TYPE;
        }
        return false;
    }

    @Override
    public void accept(TextWebSocketFrame textWebSocketFrame) {

    }
}
