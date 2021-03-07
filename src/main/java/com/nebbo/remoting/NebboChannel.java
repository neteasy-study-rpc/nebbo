package com.nebbo.remoting;

/**
 * 此对象代表一个客户端连接
 * <br/> 此处封装的目的，是因为不同的底层网络框架，这个连接的定义不同。所以做一个接口
 */
public interface NebboChannel {
    /**
     * 不需要管协议
     *
     * @param message - 发送业务数据即可
     */
    void send(byte[] message);
}
