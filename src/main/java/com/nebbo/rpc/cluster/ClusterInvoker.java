package com.nebbo.rpc.cluster;

import com.nebbo.common.tools.SpiUtils;
import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.config.RegistryConfig;
import com.nebbo.registry.NotifyListener;
import com.nebbo.registry.RegistryService;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.RpcInvocation;
import com.nebbo.rpc.protocol.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.cluster
 * FileName: ClusterInvoker
 * Author:   Administrator
 * Date:     2020/12/26 21:27
 */

public class ClusterInvoker implements Invoker {
    ReferenceConfig referenceConfig;

    /**
     * 代表这个服务能够调用的所有实例，一个invoker相当于一个长连接,
     * 客户端保存提供服务的uri
     */
    Map<URI, Invoker> invokers = new ConcurrentHashMap<>();

    LoadBalance loadBalance;

    public ClusterInvoker(ReferenceConfig referenceConfig) throws URISyntaxException {
        this.referenceConfig = referenceConfig;
        loadBalance = (LoadBalance) SpiUtils.getServiceImpl(referenceConfig.getLoadbalance(), LoadBalance.class);
        // 接口类的全类名
        String serviceName =  referenceConfig.getService().getName();
        // 1.服务发现 -- 注册中心
        List<RegistryConfig> registryConfigs = referenceConfig.getRegistryConfigs();
        for(RegistryConfig registryConfig: registryConfigs){
            String uriStr =  registryConfig.getAddress() + "/" + serviceName;
            URI registryUri = new URI(uriStr);
            RegistryService registryService =
                    (RegistryService) SpiUtils.getServiceImpl(registryUri.getScheme(), RegistryService.class);
            for(ProtocolConfig protocolConfig: referenceConfig.getProtocolConfigs()) {
                registryService.init(registryUri);
                registryService.subscribe(serviceName, new NotifyListener() {
                    // 当服务有更新(新增，剔除)时触发，
                    @Override
                    public void notify(Set<URI> changedUris) {
                        // 剔除 - 创建好的invoker，是不是存在最小的实例里面
                        Set<URI> uris =  new HashSet(changedUris);
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

    @Override
    public Class getInterface() {
        return referenceConfig.getService();
    }

    // 客户端读取响应
    @Override
    public Object invoke(RpcInvocation rpcInvocation) throws Exception {
        // invoker 调用一次 -- 这么多invokers调用哪一个。
        Invoker select = loadBalance.select(invokers);
        Object result = select.invoke(rpcInvocation);
        return result;
    }
}
