package com.nebbo.rpc.protocol.nebbo.codec;

import com.nebbo.common.serialize.Serialization;
import com.nebbo.common.tools.ByteUtil;
import com.nebbo.remoting.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.protocol.trpc.codec
 * FileName: TrpcCodec
 * Author:   Administrator
 * Date:     2020/12/26 21:30
 */
public class NebboCodec implements Codec {
    public final static byte[] MAGIC = new byte[]{(byte) 0xda, (byte) 0xbb};
    /**
     * 协议头部长度
     */
    public static final int HEADER_LEN = 6;

    // 用来临时保留没有处理过的请求报文
    ByteBuf tempMsg = Unpooled.buffer();

    // 出栈
    /**
     * 客户端 -- 编码 -- rpcinvocation
     * 服务端 -- 编码 -- response
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public byte[] encode(Object data) throws Exception {
        byte[] responseBody = (byte[]) data;
        // 构建响应
        ByteBuf responseBuffer = Unpooled.buffer();
        responseBuffer.writeByte(0xda);
        responseBuffer.writeByte(0xbb);
        responseBuffer.writeBytes(ByteUtil.int2bytes(responseBody.length));
        responseBuffer.writeBytes(responseBody);
        byte[] result = new byte[responseBuffer.readableBytes()];
        responseBuffer.readBytes(result);
        return result;
    }

    /**
     * 入栈
     * 服务端 - 解码的结果是 RpcInvocation 对象集合
     * 客户端 -- 解码结果 -- response对象集合
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public List<Object> decode(byte[] data) throws Exception {
        List<Object> out = new ArrayList<>();
        // 1. 解析(解析头部，取出数据，封装成 invocation)
        // 1.1 合并报文
        ByteBuf message = Unpooled.buffer();
        int tmpSize = tempMsg.readableBytes();
        if(tmpSize > 0 ){
            message.writeBytes(tempMsg);
            message.writeBytes(data);
            System.out.println("合并：上一数据包余下的长度为：" + tmpSize + ",合并后长度为:" + message.readableBytes());
        }else{
            message.writeBytes(data);
        }


        while(true){
            // 如果数据太少，不够一个头部，待会处理
            if(message.readableBytes() <= HEADER_LEN){
                tempMsg.clear();
                tempMsg.writeBytes(message);
                return out;
            }
            // 1.2 解析数据
            // 1.2.1 检查关键字,找到头部
            byte[] magic = new byte[2];
            message.readBytes(magic);
            for(;;){
                // 如果暂存有上一次余下的请求报文，则合并
                if(magic[0] != MAGIC[0] || magic[1] != MAGIC[1]){
                    if(message.readableBytes() == 0){
                        // 所有数据读完都没发现正确的头，算了.. 等下次数据
                        tempMsg.clear();
                        tempMsg.writeByte(magic[1]);
                        return out;
                    }
                    magic[0] = magic[1];
                    magic[1] = message.readByte();
                } else {
                    break;
                }
            }
            byte[] lengthBytes = new byte[4];
            message.readBytes(lengthBytes);
            int length = ByteUtil.Bytes2Int_BE(lengthBytes);
            // 1.2.2 读取body
            // 如果body没传输完，先不处理
            if (message.readableBytes() < length) {
                tempMsg.clear();
                tempMsg.writeBytes(magic);
                tempMsg.writeBytes(lengthBytes);
                tempMsg.writeBytes(message);
                return out;
            }
            byte[] body = new byte[length];
            message.readBytes(body);
            // 序列化
            Object o = getSerialization().deserialize(body, decodeType);
            out.add(o);
        }


    }

    @Override
    public Codec createInstance() {
        NebboCodec nebboCodec = new NebboCodec();
        nebboCodec.setDecodeType(this.decodeType);
        nebboCodec.setSerialization(this.serialization);
        return nebboCodec;
    }

    Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Serialization getSerialization() {
        return this.serialization;
    }

    Class decodeType;

    public Class getDecodeType() {
        return decodeType;
    }

    public void setDecodeType(Class decodeType) {
        this.decodeType = decodeType;
    }
}
