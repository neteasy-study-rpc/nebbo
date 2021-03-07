package com.nebbo.rpc;

/**
 * 1. 消费者调用服务，通过Invoker对象
 * 2. 服务提供者调用(TrpcServerHandler) 具体实现类，也通过Invoker对象
 */
public interface Invoker {
    /**
     * 返回接口.
     */
    Class getInterface();

    /**
     * 发起调用【负载均衡、容错、重连..都在这里面了】
     *
     * @param rpcInvocation 调用所需的参数
     * @return 执行结果
     * @throws Exception
     */
    Object invoke(RpcInvocation rpcInvocation) throws Exception;
}
