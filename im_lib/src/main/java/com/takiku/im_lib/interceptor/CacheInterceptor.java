package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.AuthException;

import java.io.IOException;

public class CacheInterceptor implements Interceptor {

    Cache cache;
    public CacheInterceptor(Cache cache){
        this.cache=cache;
    }

    @Override
    public Response intercept(Chain chain) throws IOException, InterruptedException, AuthException {
        return chain.proceed(chain.request());
    }
}
