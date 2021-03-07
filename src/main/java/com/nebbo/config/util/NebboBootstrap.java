package com.nebbo.config.util;

import com.nebbo.common.tools.SpiUtils;
import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.config.RegistryConfig;
import com.nebbo.config.ServiceConfig;
import com.nebbo.registry.RegistryService;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.cluster.ClusterInvoker;
import com.nebbo.rpc.protocol.Protocol;
import com.nebbo.rpc.proxy.ProxyFactory;

import java.net.NetworkInterface;
import java.net.URI;

public class NebboBootstrap {
    // 暴露service服务，服务端程序走这里
    public static void export(ServiceConfig serviceConfig) {
        // 1. 代理对象,代理的是提供远程服务的实现类
        Invoker invoker = ProxyFactory.getInvoker(serviceConfig.getReference(), serviceConfig.getService());
        try {
            // invoker对象
            // 2根据服务定义的协议，依次暴露。 如果有多个协议那就暴露多次
            for (ProtocolConfig protocolConfig : serviceConfig.getProtocolConfigs()) {
                // 2.1 组织URL --- 协议://ip:端口/service全类名?配置项=值&配置型2=值...
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(protocolConfig.getName() + "://");
                // 此处可选择具体网卡设备 -
                String hostAddress = NetworkInterface.getNetworkInterfaces().
                        nextElement().getInterfaceAddresses().get(0).getAddress().getHostAddress();
                stringBuilder.append(hostAddress + ":");
                stringBuilder.append(protocolConfig.getPort() + "/");
                stringBuilder.append(serviceConfig.getService().getName() + "?");
                // ....版本号啥的的不写了，意思一下吧
                stringBuilder.append("transporter=" + protocolConfig.getTransporter());
                stringBuilder.append("&serialization=" + protocolConfig.getSerialization());

                URI exportUri = new URI(stringBuilder.toString());
                System.out.println("准备暴露服务：" + exportUri);

                // 2.2 创建服务 -- 多个service 用同一个端口 TODO 思考点：一个系统，多个service需要暴露
                Protocol protocol = (Protocol) SpiUtils.getServiceImpl(protocolConfig.getName(), Protocol.class);
                protocol.export(exportUri, invoker);
//                String listenerKey = exportUri.getScheme() + exportUri.getPath() + "/providers";
                for (RegistryConfig registryConfig : serviceConfig.getRegistryConfigs()) {
                    URI registryUri = new URI(registryConfig.getAddress());
                    //getScheme()就是获取RedisRegistry://127.0.0.1:6379中的RedisRegistry
                    RegistryService registryService =
                            (RegistryService) SpiUtils.getServiceImpl(registryUri.getScheme(), RegistryService.class);
                    registryService.init(registryUri);
                    registryService.register(exportUri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个代理，用于注入
     */
    public static Object getReferenceBean(ReferenceConfig referenceConfig) {
        // 单个Invoker模式
//        try {
//            // 根据服务 通过注册中心，找到服务提供者实例
//            TrpcProtocol trpcProtocol = new TrpcProtocol();
//            Invoker invoker = trpcProtocol.refer(new URI("TrpcProtocol://127.0.0.1:8080/com.study.dubbo.sms.api.SmsService?transporter=Netty4Transporter&serialization=JsonSerialization"));
//            Object proxy =  ProxyFactory.getProxy(invoker, new Class[]{referenceConfig.getService()});
//            return proxy;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return  null;

        // Invoker集群模式
        try {
            // 根据服务 通过注册中心，找到服务提供者实例
            ClusterInvoker clusterInvoker = new ClusterInvoker(referenceConfig);
            // 代理对象
            Object proxy = ProxyFactory.getProxy(clusterInvoker, new Class[]{referenceConfig.getService()});
            return proxy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}