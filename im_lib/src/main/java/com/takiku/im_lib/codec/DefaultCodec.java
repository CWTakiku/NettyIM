package com.takiku.im_lib.codec;

import com.google.protobuf.MessageLiteOrBuilder;
import com.takiku.im_lib.codec.Codec;
import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * 默认Protobuf编解码器,请改用DefaultProtobufCodec
 */
@Deprecated
public class DefaultCodec implements Codec {
    @Override
    public ChannelHandler EnCoder() {
        return new ProtobufEncoder();
    }

    @Override
    public ChannelHandler DeCoder() {
        return new ProtobufDecoder(PackProtobuf.Pack.getDefaultInstance());
    }
}
