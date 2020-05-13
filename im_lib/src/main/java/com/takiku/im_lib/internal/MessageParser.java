package com.takiku.im_lib.internal;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.internal.handler.listener.MessageHandler;
import com.takiku.im_lib.internal.handler.listener.MessageShakeHandsHandler;
import com.takiku.im_lib.internal.handler.listener.MessageSubsequentHandler;

import java.util.ArrayList;
import java.util.List;

public class MessageParser {

    private MessageShakeHandsHandler messageShakeHandsHandler;
    private MessageSubsequentHandler messageSubsequentHandler;
    private onShakeHandsResultListener shakeHandsResultListener;
    private onSubsequentReplyListener subsequentReplyListener;

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
     *
     * @param messageSubsequentHandler
     */
   public void registerMessageSubsequentHandler(MessageSubsequentHandler messageSubsequentHandler){
       this.messageSubsequentHandler=messageSubsequentHandler;
   }
   public void addShakeResultListener(onShakeHandsResultListener shakeHandsResultListener){
       this.shakeHandsResultListener=shakeHandsResultListener;
   }
   public void addSubsequentReplyListener(onSubsequentReplyListener subsequentReplyListener){
       this.subsequentReplyListener=subsequentReplyListener;
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
                if (messageShakeHandsHandler.isShakeHandsOk((GeneratedMessageV3) msg)){
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
        for (MessageHandler messageHandler:messageHandlerList){
            if (messageHandler.isFocusMsg(msg)){
                messageHandler.handleMsg((GeneratedMessageV3) msg);
                return true;
            }
        }

        if (messageSubsequentHandler!=null){
            if (messageSubsequentHandler.isSubsequent((GeneratedMessageV3) msg)){
                if (subsequentReplyListener!=null){
                    subsequentReplyListener.subsequentReply(messageSubsequentHandler.messageTag((GeneratedMessageV3) msg),msg);
                }
                return true;
            }
        }

        return false;
    }

    public MessageShakeHandsHandler getMessageShakeHandsHandler() {
        return messageShakeHandsHandler;
    }

    public MessageSubsequentHandler getMessageSubsequentHandler() {
        return messageSubsequentHandler;
    }

    public List<MessageHandler> getMessageHandlerList() {
        return messageHandlerList;
    }

    public interface onShakeHandsResultListener{
        void shakeHandsResult(boolean isSuccess);
    }
    public interface  onSubsequentReplyListener{
        void subsequentReply(String tag,Object subsequentReplyMsg);
    }
    public interface onAckListener{
        void Ack(String tag,Object ack);
    }
}
