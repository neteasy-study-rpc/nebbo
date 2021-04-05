package com.nebbo.rpc.protocol.limiter.config;

public class RedisRateLimiterConfig {
    // 固定数值往令牌桶添加令牌
    private long permitsPerSecond;

    // 要限流的key名称
    private  String key = "limiter";

    // 个性化的返回信息
    private String mes = "系统繁忙，请稍后再试。";

    // redis服务ip
    private String host = "localhost";
    // redis服务port
    private int port = 6379;
    // redis 最大连接数
    private int maxTotal = 200;
    // redis 最大空闲连接数
    private int maxIdle = 20;
    // 获取令牌的lua脚本
    private String luaGetScript;
    // 设置令牌的lua脚本
    private String luaSetScript;
    // 定时器检查是否开启的lua脚本
    private String luaTimerScript;



    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private RedisRateLimiterConfig config;
        public Builder(){
            config = new RedisRateLimiterConfig();
        }
        public Builder permitsPerSecond(long limit) {
            config.setPermitsPerSecond(limit);
            return this;
        }
        public Builder key(String k){
            config.setKey(k);
            return this;
        }
        public Builder mes(String m){
            config.setMes(m);
            return this;
        }
        public Builder host(String h){
            config.setMes(h);
            return this;
        }
        public Builder port(int p){
            config.setPort(p);
            return this;
        }
        public Builder maxTotal(int max){
            config.setMaxTotal(max);
            return this;
        }
        public Builder maxIdle(int idle){
            config.setMaxIdle(idle);
            return this;
        }
        public Builder luaGetScript(){
            config.setLuaGetScript();
            return this;
        }

        public Builder luaSetScript(){
            config.setLuaSetScript();
            return this;
        }

        public Builder luaTimerScript(){
            config.setLuaTimerScript();
            return this;
        }
        public RedisRateLimiterConfig build(){
            return config;
        }
    }

    public long getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public void setPermitsPerSecond(long permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public String getLuaGetScript() {
        return luaGetScript;
    }

    public void setLuaGetScript() {
        this.luaGetScript = "local key = KEYS[1] \t\t\t--限流KEY\n" +
                "-- 获取当前可用令牌数\n" +
                "local current = tonumber(redis.call('get', key) or \"0\")\n" +
                "if current <= 0 then --没有令牌了\n" +
                "    return 0\n" +
                "else\n" +
                "\tredis.call(\"DECRBY\", key, \"1\") --令牌数-1\n" +
                "end\n" +
                "return 1  --返回1代表不限流";
    }

    public String getLuaSetScript() {
        return luaSetScript;
    }

    public void setLuaSetScript() {
        this.luaSetScript = "local key = KEYS[1] \t\t\t--限流KEY\n" +
                "local limit = tonumber(ARGV[1]) --容量\n" +
                "redis.call('expire', ARGV[2], 60) -- 更新定时器key的超时时间\n" +
                "-- 获取当前令牌数\n" +
                "local current = tonumber(redis.call('get', key) or \"0\")\n" +
                "if current + 1 > limit then --如果超出容量\n" +
                "    return 0\n" +
                "else\n" +
                "\tredis.call(\"INCRBY\", key, \"1\") --令牌数+1\n" +
                "end\n" +
                "return 1  --返回1代表不限流";
    }

    public String getLuaTimerScript() {
        return luaTimerScript;
    }

    public void setLuaTimerScript() {
        this.luaTimerScript = "-- 只开启一个定时器\n" +
                "local key = KEYS[1] \t\t\t--限流KEY\n" +
                "-- 判断限流key是否被设置\n" +
                "local current = redis.call('exists', key)\n" +
                "if current == 0 then --定时器没有开启\n" +
                "    redis.call(\"set\", key, \"1\") --开启定时器\n" +
                "    return 1\n" +
                "else\n" +
                "\treturn 0  -- 定时器已经开启\n" +
                "end";
    }
}
