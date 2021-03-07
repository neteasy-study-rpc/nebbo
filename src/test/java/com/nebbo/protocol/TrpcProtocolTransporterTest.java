package com.nebbo.protocol;

import com.nebbo.common.serialize.json.JsonSerialization;
import com.nebbo.remoting.transport.netty.Netty4Transporter;
import com.nebbo.rpc.RpcInvocation;
import com.nebbo.rpc.protocol.nebbo.codec.NebboCodec;
import com.nebbo.rpc.protocol.nebbo.handler.NebboServerHandler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Project: sms-service
 * Package: com.nebbo.protocol
 * FileName: TrpcProtocolTransporterTest
 * Author:   Administrator
 * Date:     2020/12/28 22:57
 */

//发出正确的协议请求
public class TrpcProtocolTransporterTest {
    public static void main(String[] args) throws URISyntaxException {
        NebboCodec nebboCodec = new NebboCodec();
        nebboCodec.setDecodeType(RpcInvocation.class);
        nebboCodec.setSerialization(new JsonSerialization());

        NebboServerHandler nebboServerHandler = new NebboServerHandler();
        new Netty4Transporter().start(new URI("TRPP://127.0.0.1:8080"),
                nebboCodec, nebboServerHandler);
    }
}
