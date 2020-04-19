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


    public PackProtobuf.Reply buildProto() {
        return PackProtobuf.Reply.newBuilder()
                .setMsgId(msgId)
                .setStatusReport(statusReport)
                .build();

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
}
