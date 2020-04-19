package com.takiku.im_lib.exception;

import java.io.IOException;

/**
 * author:chengwl
 * Description: 如果在规定时间内且重试后，服务端无响应则抛发送超时异常
 * Date:2020/4/18
 */
public class SendTimeoutException extends IOException {
    public SendTimeoutException(String message){
        super(message);
    }
}
