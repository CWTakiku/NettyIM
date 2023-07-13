package com.takiku.nettyim.framecodec;

import com.takiku.im_lib.frameCodec.FrameCodec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author chengwl
 * @des
 * @date:2023/7/12
 */
public class CustomContentFrameCodec implements FrameCodec {
    @Override
    public MessageToMessageEncoder Encoder() {
        return new CustomContentEncoder();
    }

    @Override
    public ByteToMessageDecoder Decoder() {
        return new  CustomContentDecoder();
    }

}
