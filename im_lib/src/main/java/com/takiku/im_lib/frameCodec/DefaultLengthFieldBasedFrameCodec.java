package com.takiku.im_lib.frameCodec;

import com.takiku.im_lib.frameCodec.FrameCodec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author chengwl
 * @des 默认的基于数据包长度的装包拆包
 * @date:2023/7/11
 */
public class DefaultLengthFieldBasedFrameCodec implements FrameCodec {
    private int lengthFieldLength = 2;
    private int maxFrameLength;
    public DefaultLengthFieldBasedFrameCodec(int lengthFieldLength,int maxFrameLength){
        this.lengthFieldLength  = lengthFieldLength;
        this.maxFrameLength = maxFrameLength;
    }
    @Override
    public MessageToMessageEncoder Encoder() {
        return new LengthFieldPrepender(lengthFieldLength);
    }

    @Override
    public ByteToMessageDecoder Decoder() {
        return new LengthFieldBasedFrameDecoder(maxFrameLength,
                0, lengthFieldLength, 0, lengthFieldLength);
    }
}
