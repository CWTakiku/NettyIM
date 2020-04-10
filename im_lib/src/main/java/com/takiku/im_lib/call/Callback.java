package com.takiku.im_lib.call;

import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public interface Callback {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response response) throws IOException;
}
