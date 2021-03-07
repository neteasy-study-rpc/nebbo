package com.nebbo.rpc.protocol.nebbo;

import com.nebbo.common.serialize.Serialization;
import com.nebbo.remoting.Client;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.Response;
import com.nebbo.rpc.RpcInvocation;
import com.nebbo.rpc.protocol.nebbo.handler.NebboClientHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.protocol.trpc
 * FileName: TrpcClientInvoker
 * Author:   Administrator
 * Date:     2020/12/26 21:30
 */
public class NebboClientInvoker implements Invoker {
    Client client;
    Serialization serialization;
    public NebboClientInvoker(Client client, Serialization serialization) {
        this.client = client;
        this.serialization = serialization;
    }

    @Override
    public Class getInterface() {
        return null;
    }

    // 客户端程序走这里，rpc远程调用，通过netty发送数据，并接收响应
    @Override
    public Object invoke(RpcInvocation rpcInvocation) throws Exception {
        // 1. 序列化 rpcInvocation -- 根据服务提供者的配置决定
        byte[] requestBody = serialization.serialize(rpcInvocation);
        // 2. 发起请求 -- rpcInvocation -- 协议数据包 -- 编码
        client.getChannel().send(requestBody);
        // 3.另一个线程 接收响应? TODO ? 解码--> handler
        // 实现 等待结果的
        CompletableFuture future = NebboClientHandler.waitResult(rpcInvocation.getId());
        // future.get 获取结果
        Response response = (Response) future.get(60, TimeUnit.SECONDS);
        if(response.getStatus() == 200){
            return response.getContent();
        }else{
            throw new Exception("server error:" + response.getContent().toString());
        }
    }
}
