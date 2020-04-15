package com.takiku.im_lib.internal.handler;

public interface ShakeHandsHandler  {
    /**
     * 认证是否成功
     * @param msg
     * @return
     */
    boolean isAuthOk(Object msg) ;

    boolean isShakeHands(Object msg);
}
