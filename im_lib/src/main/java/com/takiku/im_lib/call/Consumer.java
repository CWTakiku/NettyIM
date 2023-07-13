package com.takiku.im_lib.call;

/**
 * 用于特定消息的消费，例子：我发送了一个特别的消息，然后想订阅该特定消息的后续响应
 * @param <T>
 */
public interface Consumer<T extends Object> {

    /**
     *
     * @param t 接收的消息
     * @param requestTag 消息的唯一标识
     * @return
     */
    boolean Observable(T t,String requestTag);

    /**
     * 处理该消息
     * @param t
     */
    void accept(T t);
}
