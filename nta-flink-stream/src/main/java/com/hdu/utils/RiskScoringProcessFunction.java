package com.hdu.utils;

import com.hdu.entity.UnifiedLog;
import com.hdu.result.RiskResult;
import com.hdu.client.AiModelClient;
import com.hdu.client.RedisClient;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;

public class RiskScoringProcessFunction extends ProcessWindowFunction<UnifiedLog, RiskResult, String, TimeWindow> {

    @Override
    public void process(String phoneNumber,
                        Context context,
                        Iterable<UnifiedLog> logs,
                        Collector<RiskResult> out) throws Exception {

        int totalScore = 0;
        int matchCount = 0;

        for (UnifiedLog log : logs) {
            if (VPNRuleUtils.isInIocBlacklist(log))      totalScore += 30;
            if (VPNRuleUtils.isDnsForeignFailed(log))    totalScore += 20;
            if (VPNRuleUtils.isTlsSniVpn(log))           totalScore += 25;
            if (VPNRuleUtils.isConnectSensitivePorts(log)) totalScore += 15;
            matchCount++;
        }

        if (matchCount == 0) return;

        double avgScore = totalScore / (double) matchCount;
        double modelConfidence = AiModelClient.predictRiskConfidence(phoneNumber); // AI模型预测

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
        result.setModelConfidence(modelConfidence);
        result.setRiskLevel(riskLevel);
        result.setWindowStart(context.window().getStart());
        result.setWindowEnd(context.window().getEnd());

        out.collect(result);
    }
}
