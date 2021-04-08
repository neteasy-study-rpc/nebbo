package com.nebbo.config;

import java.util.ArrayList;
import java.util.List;

public class ReferenceConfig {
    private List<RegistryConfig> registryConfigs;

    private List<ProtocolConfig> protocolConfigs;

    private Class service;

    private String version;

    private String loadbalance;

    private int retryTimes;

    public List<RegistryConfig> getRegistryConfigs() {
        return registryConfigs;
    }

    public void setRegistryConfigs(List<RegistryConfig> registryConfigs) {
        this.registryConfigs = registryConfigs;
    }

    public synchronized void addRegistryConfig(RegistryConfig registryConfig) {
        if (registryConfigs == null) {
            registryConfigs = new ArrayList<RegistryConfig>();
        }
        this.registryConfigs.add(registryConfig);
    }

    public List<ProtocolConfig> getProtocolConfigs() {
        return protocolConfigs;
    }

    public void setProtocolConfigs(List<ProtocolConfig> protocolConfigs) {
        this.protocolConfigs = protocolConfigs;
    }

    public synchronized void addProtocolConfig(ProtocolConfig protocolConfig) {
        if (protocolConfigs == null) {
            protocolConfigs = new ArrayList<ProtocolConfig>();
        }
        this.protocolConfigs.add(protocolConfig);
    }

    public Class getService() {
        return service;
    }

    public void setService(Class service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
