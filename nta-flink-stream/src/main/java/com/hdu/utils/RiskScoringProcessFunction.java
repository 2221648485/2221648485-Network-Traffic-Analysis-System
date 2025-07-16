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
                        Collector<RiskResult> out) throws Exception {
//        System.out.println("窗口触发：" + phoneNumber + "，窗口时间：" + context.window());
        int totalScore = 0;
        int matchCount = 0;
        // Message保存日志信息
        HashSet<String> messages = new HashSet<>();
        for (UnifiedLog log : logs) {
            if (VPNRuleUtils.isInIocBlacklist(log)) {
                totalScore += 30;
                messages.add("命中IOC黑名单：" + log.getServerIp());
            }
            if (VPNRuleUtils.isDnsForeignFailed(log)) {
                totalScore += 20;
                messages.add("DNS连续失败，域名：" + log.getSiteUrl());
            }
            if (VPNRuleUtils.isTlsSniVpn(log)) {
                totalScore += 25;
                messages.add("SNI字段疑似VPN：" + log.getTool());
            }
            if (VPNRuleUtils.isConnectSensitivePorts(log)) {
                totalScore += 15;
                messages.add("连接敏感端口：" + log.getServerPort());
            }
            matchCount++;
        }

        if (matchCount == 0) return;

        double avgScore = totalScore / (double) matchCount;
        double modelConfidence = AiModelClient.predictRiskConfidence(logs); // AI模型预测

        String riskLevel = "low";
        String redisKey = "vpn:risk:" + phoneNumber;
        String redisCountKey = "vpn:count:" + phoneNumber;

        try (Jedis jedis = RedisClient.get()) {
            long count = jedis.incr(redisCountKey);
            jedis.expire(redisCountKey, 3600); // 设置过期时间1小时

            if (count >= 3 && avgScore > 50) riskLevel = "medium";
            if (avgScore > 70 && modelConfidence > 0.8) riskLevel = "high";

            jedis.setex(redisKey, 3600, riskLevel); // 记录风险等级，1小时过期
        }
        RiskResult result = new RiskResult();
        result.setPhoneNumber(phoneNumber);
        result.setRiskScore(avgScore);
        result.setRiskLevel(riskLevel);
        result.setWindowStart(context.window().getStart());
        result.setWindowEnd(context.window().getEnd());
        result.setMsg(String.valueOf(messages));

        out.collect(result);
    }
}
