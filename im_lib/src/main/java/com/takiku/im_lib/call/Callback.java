package com.takiku.im_lib.call;

import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

/**
 * 发送结果回调
 */
public interface Callback {
    void onFailure(Call call, IOException e); //发送失败
    void onResponse(Call call, Response response);//发送成功
}
