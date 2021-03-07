package com.nebbo.rpc.cluster.loadbalance;

import com.nebbo.rpc.Invoker;
import com.nebbo.rpc.cluster.LoadBalance;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project: sms-service
 * Package: com.nebbo.rpc.cluster.loadbalance
 * FileName: RoundRobin
 * Author:   Administrator
 * Date:     2021/1/2 19:03
 */

// 轮询负载均衡
public class RoundRobinLoadBalance implements LoadBalance {

    static AtomicInteger SEQ = new AtomicInteger();
    @Override
    public Invoker select(Map<URI, Invoker> invokerMap) {
//        if (invokerMap.values().size() ==1) {
//            return invokerMap.values().toArray(new Invoker[]{})[0];
//        }

        int index = incrementAndGet(invokerMap.size());
        System.out.println("这是轮询负载均衡器,"+index);
        return invokerMap.values().toArray(new Invoker[]{})[index];
    }

    public final int incrementAndGet(int maxValue) {
        int current;
        int next;
        do{
            current = SEQ.get();
            next = current >=maxValue-1 ? 0:current+1;
        }while(!SEQ.compareAndSet(current, next));
        return current;
    }

}
