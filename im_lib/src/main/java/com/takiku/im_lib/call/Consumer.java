package com.takiku.im_lib.call;

/**
 * 用于特定消息的消费，例子：我发送了一个特别的消息，然后想订阅该特定消息的后续响应
 * @param <T>
 */
public interface Consumer<T extends com.google.protobuf.GeneratedMessageV3> {

    boolean Observable(T t,String requestTag);
    void accept(T t);
}
