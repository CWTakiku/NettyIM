package com.takiku.im_lib.util;

public class Constants {
    public static final int MSG_STATUS_SEND = 1;//消息已送达
    public static final int MSG_STATUS_READ = 2;//消息已阅读
    public static final int MSG_STATUS_SENDING = 3;//发送中
    public static final int MSG_STATUS_WITHDRAW = 4;//消息撤回
    public static final int MSG_STATUS_FAILED = -1;//消息发送失败



    public static final int MSG_REPLY_TYPE = 0x10;
    public static final int HEART_ACK_TYPE = 0x11;
    public static final int SHAKE_HANDS_ACK_TYPE=0x12;
    public static final int MSG_ACK_TYPE = 0x13;

}
