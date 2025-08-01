package com.hdu.client;

import com.hdu.config.RedisConfig;
import com.hdu.utils.ConfigUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisClient {

    private static final RedisConfig redisConfig = ConfigUtils.getRedisConfig();
    private static final JedisPool pool = new JedisPool(redisConfig.getHost(), redisConfig.getPort());

    public static Jedis get() {
        return pool.getResource();
    }
}
