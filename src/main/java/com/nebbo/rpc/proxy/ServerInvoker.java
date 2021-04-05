package com.nebbo.rpc.proxy;

import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.RpcInvocation;
import com.nebbo.rpc.protocol.limiter.RedisRateLimiterGetter;
import com.nebbo.rpc.protocol.limiter.annotation.RedisRateLimiter;
import com.nebbo.rpc.protocol.limiter.config.RedisRateLimiterConfig;

import java.lang.reflect.Method;

/**
 * 服务实现类的代理，在NebboServerHandler类中被使用
 */
public class ServerInvoker implements Invoker {
    private Object proxy;
    private Class type;
    public ServerInvoker(Object proxy, Class type){
        this.proxy = proxy;
        this.type = type;
    }
    @Override
    public Class getInterface() {
        return type;
    }

    @Override
    public Object invoke(RpcInvocation rpcInvocation) throws Exception {
        // 反射调用对象的方法,参数proxy是具体服务提供的实现类
        Method method = proxy.getClass().getMethod(rpcInvocation.getMethodName(), rpcInvocation.getParameterTypes());
        RedisRateLimiter redisRateLimiter = method.getAnnotation(RedisRateLimiter.class);
        if(redisRateLimiter!=null && ifLimiter(redisRateLimiter)){
            // 被限流了
            String mes = redisRateLimiter.mes();
            System.out.println("没有令牌了，被限流");
            return mes;
        }
        return method.invoke(proxy, rpcInvocation.getArguments());
    }

    private boolean ifLimiter(RedisRateLimiter redisRateLimiter){

        // 执行限流操作
        long permitsPerSecond = redisRateLimiter.permitsPerSecond();
        String key = redisRateLimiter.key();
        String host = redisRateLimiter.host();
        int port = redisRateLimiter.port();
        int maxTotal = redisRateLimiter.maxTotal();
        int maxIdle = redisRateLimiter.maxIdle();
        RedisRateLimiterConfig config = RedisRateLimiterConfig.builder()
                .key(key).host(host).port(port)
                .maxTotal(maxTotal).maxIdle(maxIdle).luaGetScript().build();
        RedisRateLimiterGetter redisRateLimiterGetter = new RedisRateLimiterGetter(config);
        if(!redisRateLimiterGetter.acquire()){
            return true; // 限流
        }

        return false;
    }
}
