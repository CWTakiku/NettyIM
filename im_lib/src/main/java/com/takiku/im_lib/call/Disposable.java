package com.takiku.im_lib.call;

/**
 * 特定消息的订阅关系
 */
public interface Disposable {
    //断开
    public void dispose();
    //是否已经订阅了
    boolean isDisposed();
}
