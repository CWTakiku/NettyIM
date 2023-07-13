package com.takiku.nettyim.tcp.string;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CustomContentDecoder extends ByteToMessageDecoder {

    private static final Pattern CONTENT_PATTERN = Pattern.compile("\\[(.*?)\\]");
    StringBuilder bracketContent = new StringBuilder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 将 ByteBuf 转换为字符串
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        String input = new String(bytes);

        // 使用正则表达式匹配 [内容]
        Matcher matcher = CONTENT_PATTERN.matcher(input);
        while (matcher.find()) {
            String content = matcher.group(1);
            // 将已解码的内容添加到输出列表
            out.add(content);
        }
    }
}