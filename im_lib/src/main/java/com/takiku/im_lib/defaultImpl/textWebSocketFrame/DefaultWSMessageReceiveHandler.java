package com.takiku.im_lib.defaultImpl.textWebSocketFrame;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.defaultImpl.callback.OnMessageArriveListener;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.util.LogUtil;


import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class DefaultWSMessageReceiveHandler implements MessageHandler<TextWebSocketFrame> {
    private OnMessageArriveListener listener;
    public DefaultWSMessageReceiveHandler(OnMessageArriveListener onMessageArriveListener){
        this.listener = onMessageArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
        JsonObject jsonObject =(JsonObject) new JsonParser().parse(textWebSocketFrame.text());
        JsonElement type =   jsonObject.get("packType");
        LogUtil.i("WSMessageReceiveHandler","type "+type.getAsInt());
        return type.getAsInt() == Request.PACK_MSG_TYPE;
    }

    @Override
    public void handleMsg(TextWebSocketFrame textWebSocketFrame) {
        LogUtil.i("WSMessageReceiveHandler","type "+textWebSocketFrame.text());
        if (listener!=null){
            LogUtil.i("WSMessageReceiveHandler","type --- "+textWebSocketFrame.text());
            AppMessage appMessage= new Gson().fromJson(textWebSocketFrame.text(),AppMessage.class);
            listener.onMessageArrive(appMessage);
        }
    }


}
