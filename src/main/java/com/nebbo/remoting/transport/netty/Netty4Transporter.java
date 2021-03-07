package com.nebbo.remoting.transport.netty;

import com.nebbo.remoting.*;

import java.net.URI;

/**
 * Project: xl-rpc-all
 * Package: com.xl.rpc.remoting.netty
 * FileName: NettyTransporter
 * Author:   Administrator
 * Date:     2020/12/26 21:47
 */
public class Netty4Transporter implements Transporter {
    @Override
    public Client connect(URI uri, Codec codec, Handler handler) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect(uri, codec, handler);
        return nettyClient;
    }

    @Override
    public Server start(URI uri, Codec codec, Handler handler) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(uri, codec, handler);
        return nettyServer;
    }
}
