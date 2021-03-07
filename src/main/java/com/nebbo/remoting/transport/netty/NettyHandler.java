package com.nebbo.remoting.transport.netty;

import com.nebbo.remoting.Handler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * Project: sms-service
 * Package: com.nebbo.remoting.netty
 * FileName: NettyHandler
 * Author:   Administrator
 * Date:     2020/12/27 11:36
 */
public class NettyHandler extends ChannelDuplexHandler {
    private Handler handler;
    public NettyHandler(Handler handler){
        this.handler = handler;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("NettyHandler内容"+msg);
        handler.onReceive(new NettyChannel(ctx.channel()), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        ctx.close();
        System.out.println("异常信息：\r\n" + cause.getMessage());

    }
}
