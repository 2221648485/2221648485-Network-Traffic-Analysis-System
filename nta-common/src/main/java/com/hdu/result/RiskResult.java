package com.hdu.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {
    private String phoneNumber; // 手机号码
    private String riskLevel; // 风险等级
    private double riskScore; // 风险得分
    private LocalDateTime windowStart; // 窗口起始时间
    private LocalDateTime windowEnd; // 窗口结束时间
    private String msg; // 信息
    private LocalDateTime createTime; // 创建时间
    private String status; // 状态


    public void setWindowStart(Long time) {
        this.windowStart = parseTime(time);
    }

    public void setWindowEnd(Long time) {
        this.windowEnd = parseTime(time);
    }

    private LocalDateTime parseTime(Long time) {
        ZoneId zoneId = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zoneId);
    }
}
