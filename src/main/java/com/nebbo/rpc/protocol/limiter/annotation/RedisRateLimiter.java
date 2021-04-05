package com.nebbo.rpc.protocol.limiter.annotation;

import java.lang.annotation.*;

/**
 * 自定义限流注解
 *
 * @Author 网易云课堂微专业-java高级开发工程师【allen老师】
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RedisRateLimiter {

    // 固定数值往令牌桶添加令牌
    long permitsPerSecond() default 1L;

   // 要限流的key名称
    String key() default "limiter";

    // 个性化的返回信息
    String mes() default "系统繁忙，请稍后再试。";

    // redis服务ip
    String host() default "localhost";
    // redis服务port
    int port() default 6379;
    // redis 最大连接数
    int maxTotal() default 200;
    // redis 最大空闲连接数
    int maxIdle() default 20;

}
