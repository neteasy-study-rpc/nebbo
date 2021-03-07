package com.nebbo.rpc.cluster.loadbalance;

import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.cluster.LoadBalance;

import java.net.URI;
import java.util.Map;
import java.util.Random;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.rpc.cluster.loadbalance
 * FileName: RandomLoadBalance
 * Author:   Administrator
 * Date:     2020/12/26 21:28
 */

// 随机负载均衡
public class RandomLoadBalance implements LoadBalance {
    @Override
    public Invoker select(Map<URI, Invoker> invokerMap) {
        System.out.println("这是随机负载均衡器");
        int index = new Random().nextInt(invokerMap.values().size());
        return invokerMap.values().toArray(new Invoker[]{})[index];
    }
}
