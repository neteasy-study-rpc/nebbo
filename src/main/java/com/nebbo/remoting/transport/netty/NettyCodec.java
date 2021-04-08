package com.nebbo.remoting.transport.netty;

import com.nebbo.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Project: sms-service
 * Package: com.xl.rpc.remoting.netty
 * FileName: NettyCodec
 * Author:   Administrator
 * Date:     2020/12/27 14:43
 */
public class NettyCodec extends ChannelDuplexHandler {
    private Codec codec;

    public NettyCodec(Codec codec) {
        this.codec = codec;
    }


    // 入栈事件 （收到数据 请求/响应）
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 解码
        // 1.读取数据
        ByteBuf data = (ByteBuf) msg;
        byte[] dataBytes = new byte[data.readableBytes()];
        data.readBytes(dataBytes);

        // 2. 格式转换
        List<Object> out = codec.decode(dataBytes);
        // 3. 其他处理器继续处理，决定下一个处理器 处理数据的次数
        for(Object o : out){
            ctx.fireChannelRead(o);
        }
        ReferenceCountUtil.release(data); // 释放Bytebuf
//        System.out.println("内容"+msg);
    }
    // 出栈
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] encode = codec.encode(msg);
        super.write(ctx, Unpooled.wrappedBuffer(encode), promise);
    }
}
