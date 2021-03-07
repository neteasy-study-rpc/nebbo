package com.nebbo.registry;

import com.nebbo.registry.redis.RedisRegistry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class RedisRegistryTest {
    public static void main(String[] args) throws URISyntaxException, IOException {
        RedisRegistry redisRegistry = new RedisRegistry();
        redisRegistry.init(new URI("redis://127.0.0.1:6379"));
        redisRegistry.subscribe("com.stduy.api.TestApi", new NotifyListener() {
            @Override
            public void notify(Set<URI> uris) {
                System.out.println("redis服务更新");
                System.out.println(uris);
            }
        });
        System.in.read();
    }
}
