package com.nebbo.rpc.protocol.limiter;

import com.nebbo.rpc.protocol.limiter.config.RedisRateLimiterConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis限流，使用lua脚本，获取令牌
 */
public class RedisRateLimiterGetter {

	private RedisRateLimiterConfig config;

	private JedisPool jedisPool;


	public RedisRateLimiterGetter(RedisRateLimiterConfig config) {
		this.config = config;
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(config.getMaxTotal());
		jedisPoolConfig.setMaxIdle(config.getMaxIdle());
		jedisPool = new JedisPool(jedisPoolConfig, config.getHost(), config.getPort());

	}

	public boolean acquire() {

		try {
			Jedis jedis = jedisPool.getResource();
			return (Long) jedis.eval(config.getLuaGetScript(), 1, config.getKey()) == 1L;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

}
