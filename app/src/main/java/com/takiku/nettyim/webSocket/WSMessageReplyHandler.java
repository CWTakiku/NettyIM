package com.takiku.nettyim.webSocket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.nettyim.callbcak.OnReplyArriveListener;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class WSMessageReplyHandler implements MessageHandler<TextWebSocketFrame> {
    private OnReplyArriveListener listener;
    public WSMessageReplyHandler(OnReplyArriveListener onReplyArriveListener){
        this.listener = onReplyArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        TextWebSocketFrame textWebSocketFrame= (TextWebSocketFrame) msg;

        JsonObject jsonObject =(JsonObject) new JsonParser().parse(textWebSocketFrame.text());
        JsonElement type =   jsonObject.get("packType");
       return type.getAsInt() == Request.PACK_REPLY_TYPE;
    }

    @Override
    public void handleMsg(TextWebSocketFrame textWebSocketFrame) {
          ReplyMessage replyMessage = new Gson().fromJson(textWebSocketFrame.text(),ReplyMessage.class);
          listener.onReplyArrive(replyMessage);

    }

}
