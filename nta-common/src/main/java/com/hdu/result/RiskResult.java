package com.hdu.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {
    private String phoneNumber; // 手机号码
    private String riskLevel; // 风险等级
    private double riskScore; // 风险得分
    private long windowStart; // 窗口起始时间
    private long windowEnd; // 窗口结束时间
    private String msg; // 信息
}
