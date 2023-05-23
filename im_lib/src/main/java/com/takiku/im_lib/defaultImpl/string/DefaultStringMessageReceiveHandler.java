package com.takiku.im_lib.defaultImpl.string;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.defaultImpl.callback.OnMessageArriveListener;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;


/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class DefaultStringMessageReceiveHandler implements MessageHandler<String> {
    private OnMessageArriveListener listener;
    public DefaultStringMessageReceiveHandler(OnMessageArriveListener onMessageArriveListener){
        this.listener = onMessageArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        JsonObject jsonObject  =(JsonObject) new JsonParser().parse((String) msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_MSG_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(String msg) {
        if (listener!=null){
            AppMessage appMessage = new Gson().fromJson(msg,AppMessage.class);
            listener.onMessageArrive(appMessage);
        }
    }

}
