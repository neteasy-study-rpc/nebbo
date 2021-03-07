package com.nebbo.remoting;

import java.util.List;

/**
 * 这里等于自己要处理编解码,各种玩意 - 注意线程安全问题
 * 不同的协议，要求实现这个接口
 */
public interface Codec {
    byte[] encode(Object msg) throws Exception;

    List<Object> decode(byte[] message) throws Exception;

    Codec createInstance();
}
