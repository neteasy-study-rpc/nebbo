package com.nebbo.config;

/**
 * Project: xl-rpc-all
 * Package: com.xl.rpc.config
 * FileName: ProtocolConfig
 * Author:   Administrator
 * Date:     2020/12/26 21:19
 * 协议相关配置
 */
public class ProtocolConfig {
    private String name="NebboProtocol";
    private String port="8080";
    private String host="127.0.0.1";
    private String serialization="JsonSerialization";
    private String transporter="Netty4Transporter"; // 底层框架

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public String getTransporter() {
        return transporter;
    }

    public void setTransporter(String transporter) {
        this.transporter = transporter;
    }
}
