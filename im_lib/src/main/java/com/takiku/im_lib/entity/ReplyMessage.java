package com.takiku.im_lib.entity;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.protobuf.PackProtobuf;

import static com.takiku.im_lib.entity.base.Request.PACK_REPLY_TYPE;

public class ReplyMessage extends AbstractPack {

    public static final int STATUS_ARRIVE=1;//已送达
    public static final int STATUS_READ=2;//已阅读
    public ReplyMessage() {
        super(PACK_REPLY_TYPE);
    }

    String msgId;

    int statusReport;

    int replyType;

    String fromId;

    String toId;

    long serial;


    public static ReplyMessage buildReplyMessage(PackProtobuf.Reply reply) {
        ReplyMessage replyMessage=new ReplyMessage();
        replyMessage.setReplyType(reply.getReplyType());
        replyMessage.setFromId(reply.getFromId());
        replyMessage.setToId(reply.getToId());
        replyMessage.setMsgId(reply.getMsgId());
        replyMessage.setStatusReport(reply.getStatusReport());
        return replyMessage;
    }


    public PackProtobuf.Reply buildProto(long netId) {
        return PackProtobuf.Reply.newBuilder()
                .setMsgId(msgId)
                .setReplyType(replyType)
                .setFromId(fromId)
                .setToId(toId)
                .setStatusReport(statusReport)
                .setSerial(netId)
                .build();

    }

    public long getSerial() {
        return serial;
    }

    public String getMsgId() {
        return msgId == null ? "" : msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getStatusReport() {
        return statusReport;
    }

    public void setStatusReport(int statusReport) {
        this.statusReport = statusReport;
    }



    public int getReplyType() {
        return replyType;
    }

    public void setReplyType(int replyType) {
        this.replyType = replyType;
    }

    public String getFromId() {
        return fromId == null ? "" : fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId == null ? "" : toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }
}
