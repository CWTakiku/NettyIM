package com.takiku.im_lib.Codec;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public interface Codec  {

    MessageToMessageEncoder<MessageLiteOrBuilder> EnCoder();
    MessageToMessageDecoder<ByteBuf> DeCoder();
}
