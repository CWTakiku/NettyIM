package com.takiku.im_lib.codec;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author chengwl
 * @des tcp String 编解码器
 * @date:2023/5/23
 */
public class DefaultTcpStringCodec implements Codec{
    @Override
    public ChannelHandler EnCoder() {
        return  new StringEncoder();
    }

    @Override
    public ChannelHandler DeCoder() {
        return new StringDecoder();
    }
}
