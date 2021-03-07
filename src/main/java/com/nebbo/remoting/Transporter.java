package com.nebbo.remoting;

import java.net.URI;


/**
 * 底层网络传输 - 统一入口[服务、客户端]
 */
public interface Transporter {
    /**
     * dubbo://127.0.0.1:8080/
     * @param uri 服务器 ip，端口
     * @return
     */
    Server start(URI uri, Codec codec, Handler handler);
    // connection
    Client connect(URI uri, Codec codec, Handler handler);
}
