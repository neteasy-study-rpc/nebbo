package com.nebbo.rpc.proxy;

import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.RpcInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Project: xl-rpc-all
 * Package: com.xl.rpc.rpc.proxy
 * FileName: ProxyFactory
 * Author:   Administrator
 * Date:     2020/12/26 21:32
 */
public class ProxyFactory {
    public static Object getProxy(Invoker invoker, Class<?>[] interfaces) {
        // 返回的代理对象是new InvokerInvocationHandler(invoker)，被代理的对象是interfaces(传进来的接口)
        // 通过代理对象执行被代理对象的方法时，就会调用InvokerInvocationHandler中的invoke方法
        // 参数中的Invoker是真正提供远程服务的代理对象
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvokerInvocationHandler(invoker));
    }

    public static Invoker getInvoker(Object proxy, Class type) {
        return new Invoker() {
            @Override
            public Class getInterface() {
                return type;
            }

            @Override
            public Object invoke(RpcInvocation rpcInvocation) throws Exception {
                // 反射调用对象的方法,参数proxy是具体服务提供的实现类
                Method method = proxy.getClass().getMethod(rpcInvocation.getMethodName(), rpcInvocation.getParameterTypes());
                return method.invoke(proxy, rpcInvocation.getArguments());
            }
        };
    }
}
