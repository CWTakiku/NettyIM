package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.entity.base.AppMessage;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.protobuf.MessagePro;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

/**
 * 默认组装拦截器
 */
public class BridgeInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request=chain.request();
        if (request.getBody().getPackType()==Request.PACK_MSG_TYPE){
            AppMessage appMessage= (AppMessage) request.getBody();
            PackProtobuf.Msg msg=appMessage.build();
          request.setValue(PackProtobuf.Pack.newBuilder()
                  .setPackType(PackProtobuf.PackType.MSG)
                  .setMsg(msg)
                  .build());  ;
        }
        Response response=chain.proceed(request);
        return response;
    }
}
