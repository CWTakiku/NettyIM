package com.takiku.nettyim.udpClientDemo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;


/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class UdpMessageReceiveHandler implements MessageHandler<String> {
    private onMessageArriveListener listener;
    public UdpMessageReceiveHandler(onMessageArriveListener onMessageArriveListener){
        this.listener = onMessageArriveListener;
    }
    @Override
    public boolean isFocusMsg(Object data) {

        JsonObject jsonObject  =(JsonObject) new JsonParser().parse((String) data);
        if (jsonObject.get("packType").getAsInt() == Request.PACK_MSG_TYPE){
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(String datagramPacket) {
      //  LogUtil.i("WSMessageReceiveHandler","type "+textWebSocketFrame.text());
        if (listener!=null){
         //   LogUtil.i("WSMessageReceiveHandler","type --- "+textWebSocketFrame.text());
            listener.onMessageArrive(datagramPacket);
        }
    }
    public interface onMessageArriveListener{
        void onMessageArrive(String pack);
    }

}
