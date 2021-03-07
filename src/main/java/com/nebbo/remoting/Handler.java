package com.nebbo.remoting;

// 由具体的协议去实现
public interface Handler {
    // 收到数据 【发过来的请求、服务器给的响应】
    void onReceive(NebboChannel nebboChannel, Object message) throws Exception;

    void onWrite(NebboChannel nebboChannel, Object message) throws Exception;
}
