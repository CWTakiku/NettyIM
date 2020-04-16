package com.takiku.im_lib.entity;

import com.takiku.im_lib.entity.base.AbstractPack;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.protobuf.PackProtobuf;

public class ShakeHandsMessage extends AbstractPack {
    private String userId;
    private String token;
    private String msgId;


    public ShakeHandsMessage() {
        super(Request.PACK_HANDS_TYPE);
    }

    public void ShakeHands(){

    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PackProtobuf.ShakeHands build() {
       return PackProtobuf.ShakeHands.newBuilder()
                .setMsgId(msgId)
                .setToken(token)
                .setUserId(userId).build();
    }
}
