package com.hdu.client;

import com.hdu.entity.UnifiedLog;

public class AiModelClient {

    public static double predictRiskConfidence(Iterable<UnifiedLog> logs) {
        // 实际调用 HTTP 或本地模型预测
        // 这里模拟一个高置信度结果
        return 0.85;
    }
}
