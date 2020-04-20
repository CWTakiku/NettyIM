package com.takiku.im_lib.call;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.protobuf.GeneratedMessageV3;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

/**
 * author:chengwl
 * Description: 在UI线程回调响应结果
 * Date:2020/4/20
 */
public class UICallback<T extends GeneratedMessageV3> implements Callback {

    private Handler mDeliveryHandler;
    private OnResultListener onResultListener;

    public UICallback(OnResultListener<T> onResultListener){
        this.onResultListener=onResultListener;
        this.mDeliveryHandler= new Handler(Looper.getMainLooper());
    }
    @Override
    public void onFailure(Call call, IOException e) {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                if (onResultListener!=null)
                onResultListener.onFailure(e);
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                if (onResultListener!=null){
                    if (response!=null&&response.code==Response.SUCCESS){
                        onResultListener.onResponse(response.body);
                    }
                }
            }
        });
    }
    public interface OnResultListener<T extends GeneratedMessageV3> {
        void onFailure(IOException e);
        void onResponse(T t);
    }
}
