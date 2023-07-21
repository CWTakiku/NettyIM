package com.takiku.im_lib.internal;


import com.google.protobuf.MapEntry;
import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.call.OnResponseListener;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.internal.connection.RealConnection;
import com.takiku.im_lib.internal.connection.Stream;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.im_lib.util.LRUMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MessageParser {

    private MessageShakeHandsHandler messageShakeHandsHandler;
    private onShakeHandsResultListener shakeHandsResultListener;
    private Map<String,List<Consumer>> consumerLRUMap;
    private Map<String, OnResponseListener> onResponseListenerLRUMap;
    private List<MessageHandler> messageHandlerList=new ArrayList<>();

    public MessageParser(){

    }

    /**
     *
     * @param messageShakeHandsHandler
     */
   public void registerMessageShakeHandsHandler(MessageShakeHandsHandler messageShakeHandsHandler){
        this.messageShakeHandsHandler=messageShakeHandsHandler;
   }



    /**
     * 注册消费者，观察订阅的消息
     * @param requestTag
     * @param consumerList
     */
   public void registerConsumer(String requestTag, List<Consumer> consumerList){
       if (consumerLRUMap==null){
           consumerLRUMap= Collections.synchronizedMap(new LRUMap<>(30));
       }
       if (consumerLRUMap.containsKey(requestTag)){
           List<Consumer> old=consumerLRUMap.get(requestTag);
           old.addAll(consumerList);
           consumerLRUMap.put(requestTag,old);
       }else {
           consumerLRUMap.put(requestTag,consumerList);
       }
   }

    /**
     * 注册默认确认包消费
     * @param requestTag
     * @param ackConsumer
     */
   public void registerAckConsumer(String requestTag,Consumer ackConsumer ,OnResponseListener onResponseListener){
       if (consumerLRUMap==null){
           consumerLRUMap=Collections.synchronizedMap(new LRUMap<>(30));
       }
       if (onResponseListenerLRUMap==null){
           onResponseListenerLRUMap=Collections.synchronizedMap(new LRUMap<>(30));
       }
       if (consumerLRUMap.containsKey(requestTag)){
           List<Consumer> old=consumerLRUMap.get(requestTag);
           old.add(0,ackConsumer);
           consumerLRUMap.put(requestTag,old);
       }else {
           List<Consumer> consumerList=new ArrayList<>();
           consumerList.add(ackConsumer);
           consumerLRUMap.put(requestTag,consumerList);
       }
       onResponseListenerLRUMap.put(requestTag,onResponseListener);
   }

   public void unRegisterConsumer(String requestTag ){
       if (consumerLRUMap!=null){
           if (consumerLRUMap.containsKey(requestTag)){
               consumerLRUMap.remove(requestTag);
           }
       }
       if (onResponseListenerLRUMap!=null){
           if (onResponseListenerLRUMap.containsKey(requestTag)) {
               onResponseListenerLRUMap.remove(requestTag);
           }
       }
   }
   public boolean isRegisterConsumer(String requestTag){
       if (consumerLRUMap!=null){
           if (consumerLRUMap.containsKey(requestTag)){
             return true;
           }
       }
      return false;
   }


   public void addShakeResultListener(onShakeHandsResultListener shakeHandsResultListener){
       this.shakeHandsResultListener=shakeHandsResultListener;
   }

    /**
     *
     * @param messageHandler
     */
    public void registerMessageHandler(MessageHandler messageHandler){
        messageHandlerList.add(messageHandler);
    }
    public boolean parseMsg(Object msg){
        if (messageShakeHandsHandler!=null){
            if (messageShakeHandsHandler.isShakeHands(msg)){
                if (messageShakeHandsHandler.isShakeHandsOk( msg)){
                    if (shakeHandsResultListener!=null){
                        shakeHandsResultListener.shakeHandsResult(true);
                    }
                }else {
                    if (shakeHandsResultListener!=null){
                        shakeHandsResultListener.shakeHandsResult(false);
                    }
                }
                return true;
            }
        }
        if (consumerLRUMap!=null){
            Iterator<Map.Entry<String,List<Consumer>>> iterator=consumerLRUMap.entrySet().iterator();
           while (iterator.hasNext()){
               Map.Entry<String,List<Consumer>> entry=iterator.next();
               List<Consumer> consumers=entry.getValue();
               if (consumers.size()>0){
                   if (consumers.get(0).Observable(msg,entry.getKey())){ //是被观察的消息，则将该消息返回给所有的订阅者们
                       if (onResponseListenerLRUMap!=null&&onResponseListenerLRUMap.containsKey(entry.getKey())){
                           onResponseListenerLRUMap.get(entry.getKey()).onResponseArrive( new Response.Builder().setCode(Response.SUCCESS).build());
                       }
                       transferToSubscribers(consumers,  msg);
                       return true;
                   }
               }
           }
        }

        for (MessageHandler messageHandler:messageHandlerList){
            if (messageHandler.isFocusMsg(msg)){
                messageHandler.handleMsg(msg);
                return true;
            }
        }

        return false;
    }
    private void transferToSubscribers(List<Consumer> consumers,Object messageV3){
        for (Consumer consumer :consumers){
            consumer.accept(messageV3);
        }
    }

    public MessageShakeHandsHandler getMessageShakeHandsHandler() {
        return messageShakeHandsHandler;
    }

    public List<MessageHandler> getMessageHandlerList() {
        return messageHandlerList;
    }

    public interface onShakeHandsResultListener{
        void shakeHandsResult(boolean isSuccess);
    }

}
