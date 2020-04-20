package com.takiku.nettyim;

public class Constants {
    public static final int MSG_STATUS_SEND=1;//消息已送达
    public static final int MSG_STATUS_READ=2;//消息已阅读
    public static final int MSG_STATUS_FAILED=-1;//消息发送失败
    public static final int MSG_STATUS_SENDING=3;//发送中

    public static final int MSG_REPLY_TYPE=0x10;
}
