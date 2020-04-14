package com.takiku.im_lib.internal.connection;

import com.google.protobuf.GeneratedMessageLite;
import com.takiku.im_lib.call.Request;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;

public interface TcpStream {
    void finishRequest() throws IOException;
    void cancel();
    void writeRequest(Request request) throws IOException;
    Response readResponse(Request request);

}
