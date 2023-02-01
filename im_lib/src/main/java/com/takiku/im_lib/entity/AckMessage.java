package com.takiku.im_lib.entity;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.entity.base.Request;

/**
 * @author chengwl
 * @des
 * @date:2022/11/17
 */
public class AckMessage extends AbstractPack {
    public AckMessage() {
        super(Request.PACK_ACK_TYPE);
    }
    String msgId;
    private int ackType;

    public int getAckType() {
        return ackType;
    }

    public void setAckType(int ackType) {
        this.ackType = ackType;
    }

    public String getMsgId() {
        return msgId == null ? "" : msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
