package com.hdu.service;

import com.hdu.mapper.mysql.CockpitMysqlMapper;
import com.hdu.mapper.mysql.RiskResultMapper;
import com.hdu.vo.MetricVO;
import com.hdu.vo.VpnVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CockpitService {
    private final CockpitMysqlMapper cockpitMysqlMapper;
    private final RiskResultMapper riskResultMapper;

    public MetricVO getMetrics() {
        // 实时会话数
        int realtimeSessions = cockpitMysqlMapper.getRealtimeSessions(LocalDate.now().atStartOfDay());
        // 今日翻墙次数
        int todayCircumvention = riskResultMapper.getByLevel(Arrays.asList("medium", "low", "high"), LocalDate.now().atStartOfDay(), LocalDateTime.now());
        // 阻断次数
        int blockCount = riskResultMapper.getByLevel(Collections.singletonList("high"), LocalDate.now().atStartOfDay(), LocalDateTime.now());
        // 高危用户数
        int highRiskUsers = riskResultMapper.getByLevel(Collections.singletonList("high"), LocalDate.now().atStartOfDay(), LocalDateTime.now());
        return MetricVO.builder()
                .realtimeSessions(realtimeSessions)
                .todayCircumvention(todayCircumvention)
                .blockCount(blockCount)
                .highRiskUsers(highRiskUsers)
                .build();
    }

    public VpnVO getVpnMetrics() {
        return VpnVO.builder().vpn(cockpitMysqlMapper.getVpnInfo()).build();
    }
}
