package com.nebbo.rpc.protocol.nebbo.handler;

import com.nebbo.remoting.Handler;
import com.nebbo.remoting.NebboChannel;
import com.nebbo.rpc.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.protocol.trpc.handler
 * FileName: TrpcClientHandler
 * Author:   Administrator
 * Date:     2020/12/26 21:31
 */
public class NebboClientHandler implements Handler {
    final static Map<Long, CompletableFuture> invokerMap = new ConcurrentHashMap<>();

    // 登记一下， 创建返回一个future -- 每一个等待结果的线程一个单独的future
    public static CompletableFuture waitResult(long messageId) {
        CompletableFuture future = new CompletableFuture();
        invokerMap.put(messageId, future);
        return future;
    }

    // TODO 客户端而已，收到 响应 --- 方法执行的返回值
    // 这个方法 -- 网络框架的线程
    @Override
    public void onReceive(NebboChannel nebboChannel, Object message) throws Exception {
        Response response = (Response) message;
        // 接收所有的结果 -- 和 invoker调用者不在一个线程
        // 根据id  和 具体 的请求对应起来 complete发送
        invokerMap.get(response.getRequestId()).complete(response);
        invokerMap.remove(response.getRequestId());
    }

    @Override
    public void onWrite(NebboChannel nebboChannel, Object message) throws Exception {

    }

}
