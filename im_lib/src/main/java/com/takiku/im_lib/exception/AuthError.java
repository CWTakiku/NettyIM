package com.takiku.im_lib.exception;

import android.provider.Settings;

public class AuthError extends Throwable {
    public AuthError(String msg){
        super(msg);
    }
}
