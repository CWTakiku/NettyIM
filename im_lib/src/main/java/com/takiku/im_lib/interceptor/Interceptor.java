package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public interface Interceptor {

    Response intercept(Chain chain) throws IOException;
    interface Chain {
        Request request();

        Response proceed(Request request) throws IOException;
    }
}
