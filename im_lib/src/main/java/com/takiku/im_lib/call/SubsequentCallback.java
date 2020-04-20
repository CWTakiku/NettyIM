package com.takiku.im_lib.call;

import android.view.View;

import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/20
 */
public interface SubsequentCallback {
   void onSubsequentResponse(Response response) throws IOException;
}
