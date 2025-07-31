package com.hdu.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricVO {
    private Integer realtimeSessions; // 实时会话数
    private Integer todayCircumvention; // 今日翻墙次数
    private Integer blockCount; // 今日拦截次数
    private Integer highRiskUsers; // 高危用户数
}
