package com.takiku.im_lib.exception;

import androidx.annotation.Nullable;

import java.io.IOException;

public final class ConnectionShutdownException extends IOException {
    @Nullable
    @Override
    public String getMessage() {
        return "Connection has been disconnected";
    }
}