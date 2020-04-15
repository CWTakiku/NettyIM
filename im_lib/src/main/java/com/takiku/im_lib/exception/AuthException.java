package com.takiku.im_lib.exception;

public class AuthException extends Exception {

    public AuthException(AuthError throwable){
        super(throwable);
    }
}
