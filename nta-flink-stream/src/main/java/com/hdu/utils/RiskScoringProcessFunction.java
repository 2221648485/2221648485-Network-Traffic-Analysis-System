package com.hdu.utils;

import com.hdu.entity.UnifiedLog;
import com.hdu.result.RiskResult;
import com.hdu.client.AiModelClient;
import com.hdu.client.RedisClient;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;

import java.util.HashSet;

public class RiskScoringProcessFunction extends ProcessWindowFunction<UnifiedLog, RiskResult, String, TimeWindow> {

    @Override
    public void process(String phoneNumber,
                        Context context,
                        Iterable<UnifiedLog> logs,
                        Collector<RiskResult> out) {
//        System.out.println("窗口触发：" + phoneNumber + "，窗口时间：" + context.window());
        boolean hasHighRisk = false;
        boolean hasMediumRisk = false;
        boolean hasLowRisk = false;
        HashSet<String> messages = new HashSet<>();

        for (UnifiedLog log : logs) {
            if (VPNRuleUtils.isInIocBlacklist(log)) {
                hasHighRisk = true;
                messages.add("命中IOC黑名单IP：" + log.getServerIp());
                continue; // 高危直接判定，不再降级
            }

            if (VPNRuleUtils.isSensitiveContentAccess(log)) {
                hasHighRisk = true;
                messages.add("内容：" + log.getSiteType());
                if (log.getSiteName() != null && log.getSiteUrl() != null) {
                    messages.add("访问网站为:" + log.getSiteName() + " 网址为:" + log.getSiteUrl());
                }

                continue;
            }

            if (VPNRuleUtils.isTlsSniVpn(log)) {
                hasMediumRisk = true;
                messages.add("疑似VPN工具流量：" + log.getTool());
            } else if (VPNRuleUtils.isDnsForeignFailed(log)) {
                hasMediumRisk = true;
                messages.add("DNS异常翻墙：" + log.getSiteUrl());
            } else if (VPNRuleUtils.isConnectSensitivePorts(log)) {
                hasMediumRisk = true;
                messages.add("连接敏感端口：" + log.getServerPort());
            } else {
                hasLowRisk = true;
                messages.add("境外访问行为：" + log.getSiteUrl());
            }
        }

        // 结果输出逻辑
        String riskLevel = "None";
        if (hasHighRisk) {
            riskLevel = "high";
        } else if (hasMediumRisk) {
            riskLevel = "medium";
        } else if (hasLowRisk) {
            riskLevel = "low";
        }

        try (Jedis jedis = RedisClient.get()) {
            String redisKey = "vpn:risk:" + phoneNumber;
            String redisCountKey = "vpn:count:" + phoneNumber;

            jedis.incr(redisCountKey);
            jedis.expire(redisCountKey, 3600); // 1小时过期
            jedis.setex(redisKey, 3600, riskLevel);
        }

        RiskResult result = new RiskResult();
        result.setPhoneNumber(phoneNumber);
        result.setRiskLevel(riskLevel);
        result.setWindowStart(context.window().getStart());
        result.setWindowEnd(context.window().getEnd());
        result.setMsg(String.join(" | ", messages));

        out.collect(result);
    }
}
