package com.takiku.im_lib.codec;

import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * @author chengwl
 * @des
 * @date:2023/5/19
 */
public class DefaultProtobufCodec implements Codec{
    @Override
    public ChannelHandler EnCoder() {
        return new ProtobufEncoder();
    }

    @Override
    public ChannelHandler DeCoder() {
        return new ProtobufDecoder(PackProtobuf.Pack.getDefaultInstance());
    }
}
