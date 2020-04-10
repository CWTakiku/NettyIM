package com.takiku.im_lib.Codec;

import com.google.protobuf.MessageLite;

import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public interface Codec  {

    void EnCoder(MessageToMessageEncoder messageToMessageEncoder);
    void DeCoder(MessageToMessageDecoder messageToMessageDecoder);
}
