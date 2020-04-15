package com.takiku.im_lib.interceptor;

import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.client.IMClient;
import com.takiku.im_lib.entity.base.AppMessage;
import com.takiku.im_lib.entity.base.Response;
import com.takiku.im_lib.entity.base.ShakeHandsMessage;
import com.takiku.im_lib.exception.AuthException;
import com.takiku.im_lib.protobuf.PackProtobuf;

import java.io.IOException;

/**
 * 默认组装拦截器
 */
public class BridgeInterceptor implements Interceptor {

    IMClient imClient;
    public BridgeInterceptor(IMClient imClient){
        this.imClient=imClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException, InterruptedException, AuthException {
        Request request=chain.request();
        if (request.body.getPackType()==Request.PACK_MSG_TYPE){
            AppMessage appMessage= (AppMessage) request.body;
            PackProtobuf.Msg msg=appMessage.build();
          request.setValue(PackProtobuf.Pack.newBuilder()
                  .setPackType(PackProtobuf.Pack.PackType.MSG)
                  .setMsg(msg)
                  .build());  ;
        }else if (request.body.getPackType()==Request.PACK_HANDS_TYPE){
            ShakeHandsMessage shakeHandsMessage= (ShakeHandsMessage) request.body;
            PackProtobuf.ShakeHands shakeHands=shakeHandsMessage.build();
            request.setValue(PackProtobuf.Pack.newBuilder()
                    .setPackType(PackProtobuf.Pack.PackType.SHAKEHANDS)
                    .setShakeHands(shakeHands)
                    .build());
        }
        Response response=chain.proceed(request);
        return response;
    }
}
