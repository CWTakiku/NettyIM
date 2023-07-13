package com.takiku.im_lib.frameCodec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author chengwl
 * @des TCP的装包拆包
 * @date:2023/7/11
 */
public interface FrameCodec {
    MessageToMessageEncoder Encoder();
    ByteToMessageDecoder Decoder();
}
