package com.takiku.im_lib.internal.handler;

import com.google.protobuf.GeneratedMessageV3;

public interface ShakeHandsHandler<T extends GeneratedMessageV3>  {

    /**
     * 是否是握手包
     * @param msg
     * @return
     */
    boolean isShakeHands(Object msg);

    /**
     * 握手认证是否成功
     * @param pack
     * @return
     */
    boolean isShakeHandsOk(T pack) ;


}
