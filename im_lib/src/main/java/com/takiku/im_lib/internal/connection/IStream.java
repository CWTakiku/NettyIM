package com.takiku.im_lib.internal.connection;

import com.takiku.im_lib.call.Consumer;
import com.takiku.im_lib.entity.base.Request;
import com.takiku.im_lib.entity.base.Response;

import java.io.IOException;
import java.util.List;

public interface IStream {
    void finishRequest() throws IOException;
    void cancel();
    void writeRequest(Request request) throws IOException;
    Response readResponse(Request request);
    void registerConsumers(Request request, List<Consumer> consumers);
    void registerAckConsumer(Request request);

}
