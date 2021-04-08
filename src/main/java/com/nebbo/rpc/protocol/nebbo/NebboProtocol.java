package com.nebbo.rpc.protocol.nebbo;

import com.nebbo.common.serialize.Serialization;
import com.nebbo.common.tools.SpiUtils;
import com.nebbo.common.tools.URIUtils;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.remoting.Client;
import com.nebbo.remoting.Transporter;
import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.Response;
import com.nebbo.rpc.RpcInvocation;
import com.nebbo.rpc.protocol.Protocol;
import com.nebbo.rpc.protocol.nebbo.codec.NebboCodec;
import com.nebbo.rpc.protocol.nebbo.handler.NebboClientHandler;
import com.nebbo.rpc.protocol.nebbo.handler.NebboServerHandler;

import java.net.URI;

/**
 * Project: sms-service
 * Package: com.nebbo.rpc.protocol.Nebbo
 * FileName: NebboProtocol
 * Author:   Administrator
 * Date:     2020/12/28 0:20
 */
public class NebboProtocol implements Protocol {

    @Override
    public void export(URI exportUri, Invoker invoker) {
        // 通过spi，找到序列化的方式
        String serializationName = URIUtils.getParam(exportUri, "serialization");
        Serialization serialization = (Serialization) SpiUtils.getServiceImpl(serializationName, Serialization.class);
        // 1. 编解码器
        NebboCodec NebboCodec = new NebboCodec();
        NebboCodec.setDecodeType(RpcInvocation.class);
        NebboCodec.setSerialization(serialization);
        // 2. 创建收到请求后的处理器
        NebboServerHandler NebboServerHandler = new NebboServerHandler();
        NebboServerHandler.setInvoker(invoker);
        NebboServerHandler.setSerialization(serialization);
        // 3. 底层网络框架
        String transporterName = URIUtils.getParam(exportUri, "transporter");
        Transporter transporter = (Transporter) SpiUtils.getServiceImpl(transporterName, Transporter.class);
        // 4. 启动服务
        transporter.start(exportUri, NebboCodec, NebboServerHandler);
    }

    // 客户端程序走这里，获取netty客户端的代理对象
    @Override
    public Invoker refer(URI consumerUri, ReferenceConfig referenceConfig) {
        // 1. 找到序列化
        String serializationName = URIUtils.getParam(consumerUri, "serialization");
        Serialization serialization = (Serialization) SpiUtils.getServiceImpl(serializationName, Serialization.class);
        // 2. 编解码器
        NebboCodec codec = new NebboCodec();
        codec.setDecodeType(Response.class); // 客户端 -- 解码 -- 服务端发送过来的响应
        codec.setSerialization(serialization);
        // 3. 收到响应 处理
        NebboClientHandler NebboClientHandler = new NebboClientHandler();
        // 4. 连接 -- 长连接
        String transporterName = URIUtils.getParam(consumerUri, "transporter");
        Transporter transporter = (Transporter) SpiUtils.getServiceImpl(transporterName, Transporter.class);
        Client connect = transporter.connect(consumerUri, codec, NebboClientHandler);
        // 5. 创建一个invoker 通过网络连接发送数据
        NebboClientInvoker nebboClientInvoker = new NebboClientInvoker(connect, serialization, referenceConfig);
        return nebboClientInvoker;
    }
}
