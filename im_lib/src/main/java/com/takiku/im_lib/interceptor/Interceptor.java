package com.takiku.im_lib.interceptor;

import androidx.annotation.Nullable;

import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.dispatcher.Connection;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.exception.SendTimeoutException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface Interceptor {

    Response intercept(Chain chain) throws IOException, InterruptedException, AuthException, SendTimeoutException;
    interface Chain {

        @Nullable
        Connection connection();

        int connectTimeoutMillis();

        Chain withConnectTimeout(int timeout, TimeUnit unit);

        int sendTimeoutMillis();

        Chain withSendTimeoutMillis(int timeout, TimeUnit unit);

        Request request();

        Response proceed(Request request) throws IOException, InterruptedException, AuthException, SendTimeoutException;

        Call call();
    }
}
