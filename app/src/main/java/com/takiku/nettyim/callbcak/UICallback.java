package com.takiku.nettyim.callbcak;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.call.Call;
import com.takiku.im_lib.call.Callback;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

/**
 * author:chengwl
 * Description: 在UI线程回调响应结果
 * Date:2020/4/20
 */
public class UICallback implements Callback {

    private Handler mDeliveryHandler;
    private Callback callback;

    public UICallback(Callback callback,Handler handler){
        this.callback=callback;
        this.mDeliveryHandler= handler;
    }
    @Override
    public void onFailure(final Call call, final IOException e) {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
               callback.onFailure(call,e);
            }
        });
    }

    @Override
    public void onResponse(final Call call, final Response response) {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
              callback.onResponse(call,response);
            }
        });
    }

}
