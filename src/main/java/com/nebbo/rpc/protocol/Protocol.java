package com.nebbo.rpc.protocol;

import com.nebbo.config.ReferenceConfig;
import com.nebbo.rpc.Invoker;

import java.net.URI;

public interface Protocol {
    /**
     * 开放服务
     * @param exportUri 协议名称://IP:端口/service全类名?参数名称=参数值&参数1名称=参数2值
     * @param invoker 调用具体实现类的代理对象
     */
    public void export(URI exportUri, Invoker invoker);

    /**
     * 获取一个协议所对应的invoker，用于调用
     * @param consumerUri
     * @return
     */
    public Invoker refer(URI consumerUri, ReferenceConfig referenceConfig);
}
