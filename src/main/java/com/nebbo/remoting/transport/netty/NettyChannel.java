package com.nebbo.remoting.transport.netty;

import com.nebbo.remoting.NebboChannel;
import io.netty.channel.Channel;

public class NettyChannel implements NebboChannel {

    Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    // 负责向channel中写数据，客户端和服务端都可以调用
    @Override
    public void send(byte[] message) {
        channel.writeAndFlush(message);
    }
}
