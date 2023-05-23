package com.takiku.im_lib.defaultImpl.string;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.defaultImpl.callback.OnReplyArriveListener;
import com.takiku.im_lib.entity.ReplyMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;


/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class DefaultStringMessageReplyHandler implements MessageHandler<String> {
    public DefaultStringMessageReplyHandler(OnReplyArriveListener onReplyArriveListener){
        this.listener = onReplyArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object msg) {
        JsonObject jsonObject  =(JsonObject) new JsonParser().parse((String) msg);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_REPLY_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(String msg) {
          ReplyMessage replyMessage = new Gson().fromJson(msg,ReplyMessage.class);
          listener.onReplyArrive(replyMessage);

    }

    private OnReplyArriveListener listener;
}
