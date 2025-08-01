package com.hdu.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {
    private Long id;
    private String phoneNumber; // 手机号码
    private String riskLevel; // 风险等级
    private LocalDateTime windowStart; // 窗口起始时间
    private LocalDateTime windowEnd; // 窗口结束时间
    private String msg; // 信息
    private LocalDateTime createTime; // 创建时间
    private String status; // 状态
    private String adslAccount; // ADSL账号


    public void setWindowStartTime(Long time) {
        this.windowStart = parseTime(time);
    }

    public void setWindowEndTime(Long time) {
        this.windowEnd = parseTime(time);
    }

    private LocalDateTime parseTime(Long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }
}
