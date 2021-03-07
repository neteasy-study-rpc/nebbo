package com.nebbo.rpc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Project: sms-service
 * Package: com.xl.rpc.rpc
 * FileName: RpcInvocation
 * Author:   Administrator
 * Date:     2020/12/27 14:26
 */
public class RpcInvocation implements Serializable {
    private static final long serialVersionUID = -4355285085441097045L;
    static AtomicLong SEQ = new AtomicLong();
    private long id;
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;

    public RpcInvocation() {
        // 初始化一个ID
        this.setId(incrementAndGet());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
    }


    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    @Override
    public String toString() {
        return "RpcInvocation [id="+ id +",methodName=" + methodName + ", parameterTypes="
                + Arrays.toString(parameterTypes) + ", arguments=" + Arrays.toString(arguments)
                + "]";
    }

    public final long incrementAndGet() {
        long current;
        long next;
        do {
            current = SEQ.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!SEQ.compareAndSet(current, next));

        return next;
    }
}
