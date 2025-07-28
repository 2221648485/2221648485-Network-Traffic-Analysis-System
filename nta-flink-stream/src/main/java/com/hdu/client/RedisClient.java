package com.hdu.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisClient {
    private static final JedisPool pool = new JedisPool("localhost", 6379);
//    private static final JedisPool pool = new JedisPool("redis", 6379);

    public static Jedis get() {
        return pool.getResource();
    }
}
