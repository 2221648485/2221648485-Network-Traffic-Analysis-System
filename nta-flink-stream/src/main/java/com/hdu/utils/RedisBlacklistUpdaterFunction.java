package com.hdu.utils;

import com.hdu.entity.BlacklistStore;
import com.hdu.entity.UnifiedLog;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisBlacklistUpdaterFunction extends RichFlatMapFunction<UnifiedLog, UnifiedLog> {

    private transient Jedis jedis;
    private transient ScheduledExecutorService scheduler;

    // Redis 配置
    private final String redisHost = "localhost";
    private final int redisPort = 6379;

    // 黑名单 Redis Key
    private final String ipBlacklistKey = "ioc:ip";
    private final String domainBlacklistKey = "ioc:domain";

    // 拉取周期，单位秒
    private final long refreshPeriodSeconds = 10000*300L;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // System.out.println("[RedisBlacklistUpdater] 实例初始化，任务ID: " + getRuntimeContext().getTaskNameWithSubtasks());
        jedis = new Jedis(redisHost, redisPort);
        // jedis.auth("123456");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshBlacklistCache, 0, refreshPeriodSeconds, TimeUnit.SECONDS);
    }

    private void refreshBlacklistCache() {
        try {
            // 拉取IP黑名单
            Set<String> ipSet = jedis.smembers(ipBlacklistKey);
            if (ipSet != null && !ipSet.isEmpty()) {
                List<String> ipList = ipSet.stream().collect(Collectors.toList());
                BlacklistStore.updateIps(ipList);
                System.out.println("[RedisBlacklistUpdater] IP 黑名单更新，数量：" + ipList.size());
            } else {
                System.out.println("[RedisBlacklistUpdater] Redis IP 黑名单为空或获取失败");
            }

            // 拉取域名黑名单
            Set<String> domainSet = jedis.smembers(domainBlacklistKey);
            if (domainSet != null && !domainSet.isEmpty()) {
                List<String> domainList = domainSet.stream().collect(Collectors.toList());
                BlacklistStore.updateHostnames(domainList);
                System.out.println("[RedisBlacklistUpdater] 域名黑名单更新，数量：" + domainList.size());
            } else {
                System.out.println("[RedisBlacklistUpdater] Redis 域名黑名单为空或获取失败");
            }
        } catch (Exception e) {
            System.err.println("[RedisBlacklistUpdater] 刷新黑名单失败：" + e.getMessage());
        }
    }

    @Override
    public void flatMap(UnifiedLog value, Collector<UnifiedLog> out) throws Exception {
        out.collect(value);
    }

    @Override
    public void close() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (jedis != null) {
            jedis.close();
        }
        super.close();
    }
}
