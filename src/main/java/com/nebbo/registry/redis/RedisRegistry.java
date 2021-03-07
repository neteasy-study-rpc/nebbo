package com.nebbo.registry.redis;

import com.nebbo.common.tools.URIUtils;
import com.nebbo.registry.NotifyListener;
import com.nebbo.registry.RegistryService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisRegistry implements RegistryService {
    private static final int TIME_OUT = 15; // 15秒过期
    URI address;
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);

    // 服务提供者相关
    ArrayList<URI> servicesHeartBeat = new ArrayList<>();

    // 服务消费者相关
    JedisPubSub jedisPubSub;
    Map<String, Set<URI>> localCache = new ConcurrentHashMap<>();
    Map<String, NotifyListener> listenerMap = new ConcurrentHashMap<>();

    // trpc-TrpcProtocol://127.0.0.1:10088/com.study.dubbo.sms.api.SmsService?transporter=Netty4Transporter&serialization=JsonSerialization
    @Override
    public void register(URI uri) {
        String key = "nebbo-" + uri.toString();
        Jedis jedis = new Jedis(address.getHost(), address.getPort());
        jedis.setex(key, TIME_OUT, String.valueOf(System.currentTimeMillis()));
        jedis.close();
        // 开始心跳
        servicesHeartBeat.add(uri);
    }

    @Override
    public void subscribe(String service, NotifyListener notifyListener) {
        try {
            if (localCache.get(service) == null) {
                localCache.putIfAbsent(service, new HashSet<>());
                listenerMap.putIfAbsent(service, notifyListener);
                // 第一次直接获取
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                String key = "nebbo-" + service;
                Set<String> serviceInstances = jedis.keys("nebbo-*" + service + "?*");
                for (String instances : serviceInstances) {
                    URI instanceUri = new URI(instances.replace("nebbo-", ""));
                    localCache.get(service).add(instanceUri);
                }
                notifyListener.notify(localCache.get(service));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(URI address) {
        this.address = address;

        // 启动定时任务
        executorService.scheduleWithFixedDelay((Runnable) () -> {
            try {
                // -- 心跳 -- 延长时间
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                for (URI service : servicesHeartBeat) {
                    String key = "nebbo-" + service.toString();
                    System.out.println("flush key:"+key);
                    jedis.expire(key, TIME_OUT);
                }
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 3000, 5000, TimeUnit.MILLISECONDS);

        // 监听服务变动 - redis.conf  notify-keyspace-events KE$xg
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    jedisPubSub = new JedisPubSub() {
                        @Override
                        public void onPSubscribe(String pattern, int subscribedChannels) {
                            // 订阅者开始监听，模式匹配方式
                            System.out.println("注册中心开始监听:" + pattern);
                        }

                        @Override
                        public void onPMessage(String pattern, String channel, String message) {
                            try {
                                URI serviceURI = new URI(channel.replace("__keyspace@0__:nebbo-", ""));
                                if ("set".equals(message)) {
                                    // 新增
                                    Set<URI> uris = localCache.get(URIUtils.getService(serviceURI));
                                    if (uris != null) {
                                        uris.add(serviceURI);
                                    }
                                }
                                if ("expired".equals(message)) {
                                    // 过期
                                    Set<URI> uris = localCache.get(URIUtils.getService(serviceURI));
                                    if (uris != null) {
                                        uris.remove(serviceURI);
                                    }
                                }
                                if ("set".equals(message) || "expired".equals(message)) {
                                    System.out.println("服务实例有变化，开始刷新");
                                    NotifyListener notifyListener = listenerMap.get(URIUtils.getService(serviceURI));
                                    if (notifyListener != null) {
                                        notifyListener.notify(localCache.get(URIUtils.getService(serviceURI)));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Jedis jedis = new Jedis(address.getHost(), address.getPort());
                    jedis.psubscribe(jedisPubSub, "__keyspace@0__:nebbo-*");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
