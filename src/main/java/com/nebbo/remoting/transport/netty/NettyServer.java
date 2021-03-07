package com.nebbo.remoting.transport.netty;

import com.nebbo.remoting.Codec;
import com.nebbo.remoting.Handler;
import com.nebbo.remoting.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.remoting.netty
 * FileName: NettyServer
 * Author:   Administrator
 * Date:     2020/12/26 21:26
 */
public class NettyServer implements Server {
    //开启一个网络服务，创建事件循环组
    EventLoopGroup boss = new NioEventLoopGroup(); //处理连接的线程池
    EventLoopGroup worker = new NioEventLoopGroup(); //处理io的线程池

    @Override
    public void start(URI uri, Codec codec, Handler handler) {
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    // 指定所使用的nio传输channel
                    .channel(NioServerSocketChannel.class)
                    // 指定要监听地址
                    .localAddress(new InetSocketAddress(uri.getHost(),uri.getPort()))
                    // 添加handler - 有链接之后 处理逻辑
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 协议编解码 （RpcInvocation）
                            // codec.createInstance()让每条职责链都有自己的编解码器，避免线程安全问题
                            ch.pipeline().addLast(new NettyCodec(codec.createInstance()));
                            // 具体的逻辑执行
                            ch.pipeline().addLast(new NettyHandler(handler));
                        }
                    });
            ChannelFuture future  = bootstrap.bind().sync();
            System.out.println("完成端口绑定和服务器启动");
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
