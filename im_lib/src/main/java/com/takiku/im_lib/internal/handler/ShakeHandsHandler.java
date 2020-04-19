package com.takiku.im_lib.internal.handler;

public interface ShakeHandsHandler  {

    /**
     * 是否是握手包
     * @param msg
     * @return
     */
    boolean isShakeHands(Object msg);

    /**
     * 握手认证是否成功
     * @param msg
     * @return
     */
    boolean isShakeHandsOk(Object msg) ;


}
