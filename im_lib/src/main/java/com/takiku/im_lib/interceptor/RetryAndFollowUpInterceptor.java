package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public class RetryAndFollowUpInterceptor implements Interceptor {

    private final IMClient client;

    public RetryAndFollowUpInterceptor(IMClient imClient){
        this.client=imClient;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {

        return null;
    }
}
