package com.takiku.im_lib.call;

import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public interface Callback<T extends com.google.protobuf.GeneratedMessageV3 > {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response<T> response) throws IOException;
}
