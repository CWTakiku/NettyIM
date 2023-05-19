package com.takiku.im_lib.codec;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author chengwl
 * @des
 * @date:2023/5/19
 */
public class DefaultUdpStringCodec implements Codec{

    private Charset charset;
    public void DefaultProtobufCodec(Charset charset){
        this.charset = charset;
    }

    @Override
    public ChannelHandler EnCoder() {
        return new MyEncoder(null);
    }

    @Override
    public ChannelHandler DeCoder() {
        return new MyDecoder();
    }

    public  class MyDecoder extends MessageToMessageDecoder<DatagramPacket> {
        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
            // 读取数据
            ByteBuf data = packet.content();

            // 将字节数据转换为字符串
            String str = data.toString(charset);

            // 将解码后的字符串添加到输出列表中
            out.add(str);
        }
    }
    public  class MyEncoder extends MessageToMessageEncoder<Object> {
        private InetSocketAddress remoteAddress;

        public MyEncoder(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }


        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            // 将字符串转换为字节数组
            if (msg instanceof String){
                byte[] bytes = ((String)msg).getBytes(charset);
                // 创建DatagramPacket对象
                DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(bytes), remoteAddress);

                // 将编码后的DatagramPacket添加到输出列表中
                out.add(packet);
            }



        }
    }
}
