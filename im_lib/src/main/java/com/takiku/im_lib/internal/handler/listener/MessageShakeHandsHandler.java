package com.takiku.im_lib.internal.handler.listener;

import com.google.protobuf.GeneratedMessageV3;

public interface MessageShakeHandsHandler<K extends Object,T extends Object>  {


    /**
     * 发送给服务端的握手包
     * @return
     */
    K ShakeHands();

    /**
     * 是否是握手包回应包     * @param msg
     * @return
     */
    boolean isShakeHands(Object msg);

    /**
     * 客户端端自己判断返回的握手认证回应包是否成功
     * @param pack
     * @return
     */
    boolean isShakeHandsOk(T pack) ;


}
