package com.nebbo.registry;

import java.net.URI;

public interface RegistryService {
    /**
     * 注册
     *
     * @param uri
     */
    public void register(URI uri);

    /**
     * 订阅指定服务
     */
    public void subscribe(String service, NotifyListener notifyListener);

    /**
     * 配置连接信息
     *
     * @param address
     */
    public void init(URI address);
}
