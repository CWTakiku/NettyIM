package com.takiku.im_lib.interceptor;

import android.telecom.StatusHints;

import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public class CacheInterceptor implements Interceptor {

    Cache cache;
    public CacheInterceptor(Cache cache){
        this.cache=cache;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
