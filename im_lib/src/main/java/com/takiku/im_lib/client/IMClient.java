package com.takiku.im_lib.client;

import androidx.annotation.Nullable;

import com.takiku.im_lib.authenticator.Authenticator;
import com.takiku.im_lib.cache.Cache;
import com.takiku.im_lib.dispatcher.Dispatcher;
import com.takiku.im_lib.interceptor.MsgInterceptor;

import java.util.ArrayList;
import java.util.List;

public class IMClient {



    public IMClient(){this(new Builder());}


    IMClient(Builder builder){

    }

    public static final class Builder{
     Dispatcher dispatcher;
     final List<MsgInterceptor> interceptors = new ArrayList<>();
     @Nullable Cache cache;
     Authenticator authenticator;

    }
}
