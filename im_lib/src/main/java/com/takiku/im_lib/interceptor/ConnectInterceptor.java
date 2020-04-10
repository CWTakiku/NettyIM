package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public class ConnectInterceptor implements Interceptor {

    public ConnectInterceptor(IMClient imClient){

    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
