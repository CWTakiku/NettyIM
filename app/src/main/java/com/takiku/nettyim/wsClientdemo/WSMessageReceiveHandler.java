package com.takiku.nettyim.wsClientdemo;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class WSMessageReceiveHandler implements MessageHandler<TextWebSocketFrame> {
    private onMessageArriveListener listener;
    public WSMessageReceiveHandler(onMessageArriveListener onMessageArriveListener){
        this.listener = onMessageArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
        JsonObject jsonObject =(JsonObject) new JsonParser().parse(textWebSocketFrame.text());
        JsonElement type =   jsonObject.get("packType");
        Log.i("WSMessageReceiveHandler","type "+type.getAsInt());
        return type.getAsInt() == Request.PACK_MSG_TYPE;
    }

    @Override
    public void handleMsg(TextWebSocketFrame textWebSocketFrame) {
        Log.i("WSMessageReceiveHandler","type "+textWebSocketFrame.text());
        if (listener!=null){
            Log.i("WSMessageReceiveHandler","type --- "+textWebSocketFrame.text());
            listener.onMessageArrive(textWebSocketFrame);
        }
    }
    public interface onMessageArriveListener{
        void onMessageArrive(TextWebSocketFrame pack);
    }

}
