package com.nebbo.rpc.proxy;

import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvokerInvocationHandler implements InvocationHandler {
    private final Invoker invoker;

    public InvokerInvocationHandler(Invoker handler) {
        this.invoker = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 不需要远程调用,直接invoker调用
        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return invoker.toString();
            } else if ("$destroy".equals(methodName)) {
                return null;
            } else if ("hashCode".equals(methodName)) {
                return invoker.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return invoker.equals(args[0]);
        }
        // 需要远程调用的方法，method, invoker.getInterface().getName(), args;
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setMethodName(methodName);
        rpcInvocation.setArguments(args);
        rpcInvocation.setParameterTypes(parameterTypes);
        rpcInvocation.setServiceName(method.getDeclaringClass().getName());
        return invoker.invoke(rpcInvocation);
    }
}
