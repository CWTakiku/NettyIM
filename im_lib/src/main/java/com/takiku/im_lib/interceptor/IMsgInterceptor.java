package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.protobuf.MessagePro;

import java.io.IOException;

/**
 * create by cwl
 * 消息拦截器
 */
public interface IMsgInterceptor {
   MessagePro msgInterceptor(Chain chain) ;
   interface Chain {
      MessagePro request();

      MessagePro proceed(MessagePro request) throws IOException;
   }
}
