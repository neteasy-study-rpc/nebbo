package com.nebbo.rpc.protocol.limiter;

import com.nebbo.rpc.protocol.limiter.config.RedisRateLimiterConfig;
import redis.clients.jedis.Jedis;

public class RedisRateLimiterSetter implements AutoCloseable, Runnable{

	private RedisRateLimiterConfig config;

	private String keyTimer; // 最先启动的服务来负责令牌的生成，保证全局唯一

	private Jedis jedis;

	public RedisRateLimiterSetter(RedisRateLimiterConfig config) {
		this.config = config;
		keyTimer = config.getKey() + "_timer";
		jedis = new Jedis(config.getHost(), config.getPort());
	}

	@Override
	public void close() throws Exception {
		this.jedis.close();
	}

	@Override
	public void run() {

		// 通过定时器，定时放入令牌
		Long flag = (Long) jedis.eval(config.getLuaSetScript(), 1, config.getKey(),
				String.valueOf(config.getPermitsPerSecond()), keyTimer);
		if(flag==1L){
			System.out.println("向令牌桶"+config.getKey()+"加入一个令牌");
		}

	}

	public boolean isStartTimer(){
		Long flag = (Long) jedis.eval(config.getLuaTimerScript(), 1, keyTimer);
		if(flag==1L){
			return true; // 可以开启定时器
		}else{
			return false; // 定时器已经开启
		}
	}

}
