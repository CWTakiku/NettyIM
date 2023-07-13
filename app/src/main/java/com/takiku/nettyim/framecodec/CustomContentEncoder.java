package com.takiku.nettyim.framecodec;

import com.takiku.im_lib.util.LogUtil;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author chengwl
 * @des
 * @date:2023/7/12
 */
public class CustomContentEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        LogUtil.i("test","CustomContentEncoder "+msg);
        ByteBuf directBuffer = Unpooled.directBuffer();
        directBuffer.writeByte('[');
        directBuffer.writeBytes(msg);
        directBuffer.writeByte(']');
        out.add(directBuffer);
    }
}
