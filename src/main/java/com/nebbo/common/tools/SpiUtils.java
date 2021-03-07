package com.nebbo.common.tools;

import java.util.ServiceLoader;

public class SpiUtils {
    public static Object getServiceImpl(String serviceName, Class classType) {
        ServiceLoader services = ServiceLoader.load(classType, Thread.currentThread().getContextClassLoader());
        // 根据服务定义的协议，依次暴露。 如果有多个协议那就暴露多次
        for (Object s : services) {
            if (s.getClass().getSimpleName().equals(serviceName)) {
                return s;
            }
        }
        return null;
    }
}
