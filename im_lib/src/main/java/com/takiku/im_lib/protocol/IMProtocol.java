package com.takiku.im_lib.protocol;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author chengwl
 * @des
 * @date:2022/11/11
 */
@IntDef({IMProtocol.PRIVATE,IMProtocol.WEB_SOCKET})
@Retention(RetentionPolicy.SOURCE)
public @interface IMProtocol {
    int PRIVATE = 0;
    int WEB_SOCKET = 1;
}
