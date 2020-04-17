package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.AppMessage;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.entity.ShakeHandsMessage;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

/**
 * 默认装配拦截器拦截器
 */
public class BridgeInterceptor implements Interceptor {

    IMClient imClient;
    public BridgeInterceptor(IMClient imClient){
        this.imClient=imClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException, InterruptedException, AuthException {
        Request request=chain.request();

        Response response=chain.proceed(request);
        return response;
    }
}
