package com.takiku.im_lib.entity;


import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.protobuf.PackProtobuf;
import com.takiku.im_lib.util.StringUtil;

public  class AppMessage extends AbstractPack {
    public static final int MSG_REPLY_TYPE=1001;
    public static final int MSG_SENDED=1;

    private Head head;  // 消息头
    private String body;// 消息体
    public AppMessage(Builder builder){
        super(Request.PACK_MSG_TYPE);
        this.head=builder.head;
        this.body=builder.body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public String getBody() {
        return body == null ? "" : body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static class Builder{
        private Head head=new Head();  // 消息头
        private String body;// 消息体
        public Builder setMsgId(String msgId){
              head.setMsgId(msgId);
              return this;
        }
        public Builder setFromId(String fromId){
            head.setFromId(fromId);
            return this;
        }

        public Builder setToId(String toId){
            head.setToId(toId);
            return this;
        }

        public Builder setMsgType(int msgType){
            head.setMsgType(msgType);
            return this;
        }

        public Builder setTimestamp(long timestamp){
            head.setTimestamp(timestamp);
            return this;
        }

        public Builder setExtend(String extend){
            head.setExtend(extend);
            return this;
        }

        public Builder setMsgContentType(int msgContentType){
            head.setMsgContentType(msgContentType);
            return this;
        }
        public Builder setBody(String body){
            this.body=body;
            return this;
        }

        public AppMessage build(){
            return new AppMessage(this);
        }

    }

    public PackProtobuf.Msg buildProto() {
        PackProtobuf.Msg.Builder builder = PackProtobuf.Msg.newBuilder();
        PackProtobuf.Head.Builder headBuilder = PackProtobuf.Head.newBuilder();
        headBuilder.setMsgType(getHead().getMsgType());
        headBuilder.setMsgContentType(getHead().getMsgContentType());
        if (!StringUtil.isEmpty(getHead().getMsgId()))
            headBuilder.setMsgId(getHead().getMsgId());
        if (!StringUtil.isEmpty(getHead().getFromId()))
            headBuilder.setFromId(getHead().getFromId());
        if (!StringUtil.isEmpty(getHead().getToId()))
            headBuilder.setToId(getHead().getToId());
        if (getHead().getTimestamp() != 0)
            headBuilder.setTimestamp(getHead().getTimestamp());
        if (!StringUtil.isEmpty(getHead().getExtend()))
            headBuilder.setExtend(getHead().getExtend());
        if (!StringUtil.isEmpty(getBody()))
            builder.setBody(getBody());
        builder.setHead(headBuilder);
        return builder.build();
    }
}
