package com.nebbo.registry.integration;

import com.nebbo.common.tools.SpiUtils;
import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.config.RegistryConfig;
import com.nebbo.registry.NotifyListener;
import com.nebbo.registry.RegistryService;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.protocol.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * RegistryDirectory 目录的意思，简单理解：就是服务列表-- 订阅注册中心对应的消息，及时刷新本地的服务实例列表
 */
public class RegistryDirectory {
    Map<URI, Invoker> invokers = new ConcurrentHashMap<>();
    private ReferenceConfig referenceConfig;
    public RegistryDirectory(ReferenceConfig referenceConfig) throws URISyntaxException {
        this.referenceConfig = referenceConfig;
        // 接口类的全类名
        String serviceName =  referenceConfig.getService().getName();

        // 1.服务发现 -- 注册中心
        List<RegistryConfig> registryConfigs = referenceConfig.getRegistryConfigs();
        for(RegistryConfig registryConfig: registryConfigs){
            URI registryUri = new URI(registryConfig.getAddress());
            RegistryService registryService =
                    (RegistryService) SpiUtils.getServiceImpl(registryUri.getScheme(), RegistryService.class);

            for(ProtocolConfig protocolConfig:referenceConfig.getProtocolConfigs()) {
                registryService.init(registryUri);
                registryService.subscribe(serviceName, new NotifyListener() {
                    // 当服务有更新(新增，剔除)时触发，
                    @Override
                    public void notify(Set<URI> uris) {
                        // 剔除 - 创建好的invoker，是不是存在最小的实例里面
                        System.out.println("更新前的服务invoker信息" + invokers);
                        for (URI uri : invokers.keySet()) {
                            if (!uris.contains(uri)) {
                                invokers.remove(uri);
                            }
                        }

                        // 新增 - 意味新建一个invoker
                        for (URI uri : uris) {
                            if (!invokers.containsKey(uri)) {
                                // 一个暴露服务的uri，意味着一个服务实例
                                Protocol protocol = (Protocol) SpiUtils.getServiceImpl(uri.getScheme(), Protocol.class);
                                Invoker invoker = protocol.refer(uri); // invoker代表一个长连接
                                // 保存起来
                                invokers.putIfAbsent(uri, invoker);
                            }
                        }
                        System.out.println("更新后的服务invoker信息" + invokers);
                    }
                });
            }
        }
    }

    public Map<URI, Invoker> getInvokers() {
        return invokers;
    }
}
