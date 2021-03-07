package com.nebbo.rpc.protocol.nebbo.handler;

import com.nebbo.common.serialize.Serialization;
import com.nebbo.remoting.Handler;
import com.nebbo.remoting.NebboChannel;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.Response;
import com.nebbo.rpc.RpcInvocation;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.protocol.nebbo.handler
 * FileName: TrpcServerHandler
 * Author:   Administrator
 * Date:     2020/12/26 21:31
 */
public class NebboServerHandler implements Handler {
    // message 就是 rpcinvocation
    @Override
    public void onReceive(NebboChannel nebboChannel, Object message) throws Exception {
        RpcInvocation rpcInvocation = (RpcInvocation) message;
        System.out.println("收到rpcInvocation信息：" + rpcInvocation);
        // TODO 发起方法调用 -- 谁？
        // 发出数据 -- response
        Response response = new Response();
        try {
            // 调用目标 接口实现类
            Object result = getInvoker().invoke(rpcInvocation);
            response.setRequestId(rpcInvocation.getId());
            response.setStatus(200);
            response.setContent(result);
            System.out.println("服务端执行结果：" + result);
        } catch (Throwable e) {
            response.setStatus(500);
            response.setContent(e.getMessage());
            e.printStackTrace();
        }
        // 发送数据
        byte[] responseBody = getSerialization().serialize(response);
        nebboChannel.send(responseBody); // write方法

    }

    @Override
    public void onWrite(NebboChannel nebboChannel, Object message) throws Exception {

    }
    Invoker invoker;

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Invoker getInvoker() {
        return this.invoker;
    }

    Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Serialization getSerialization() {
        return this.serialization;
    }

}
