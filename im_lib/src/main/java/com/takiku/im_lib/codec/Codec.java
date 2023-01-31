package com.takiku.im_lib.codec;

import com.google.protobuf.MessageLiteOrBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public interface Codec  {

    ChannelHandler EnCoder();
    ChannelHandler DeCoder();
}
