package com.takiku.im_lib.codec;

import com.takiku.im_lib.protobuf.PackProtobuf;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * @author chengwl
 * @des tcp Protobuf 编解码器
 * @date:2023/5/19
 */
public class DefaultTcpProtobufCodec implements Codec{
    @Override
    public ChannelHandler EnCoder() {
        return new ProtobufEncoder();
    }

    @Override
    public ChannelHandler DeCoder() {
        return new ProtobufDecoder(PackProtobuf.Pack.getDefaultInstance());
    }
}
