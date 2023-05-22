package com.takiku.nettyim.udp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.nettyim.callbcak.OnMessageArriveListener;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class UdpMessageReceiveHandler implements MessageHandler<String> {
    private OnMessageArriveListener listener;
    public UdpMessageReceiveHandler(OnMessageArriveListener onMessageArriveListener){
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
      //  LogUtil.i("WSMessageReceiveHandler","type "+textWebSocketFrame.text());
        if (listener!=null){
         //   LogUtil.i("WSMessageReceiveHandler","type --- "+textWebSocketFrame.text());
            AppMessage appMessage = new Gson().fromJson(msg,AppMessage.class);
            listener.onMessageArrive(appMessage);
        }
    }


}
