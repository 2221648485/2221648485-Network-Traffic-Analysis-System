package com.hdu.service;

import com.hdu.mapper.mysql.RiskResultMapper;
import com.hdu.mapper.sqlserver.CockpitMapper;
import com.hdu.vo.MetricVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CockpitService {
    private final CockpitMapper cockpitMapper;
    private final RiskResultMapper riskResultMapper;

    public MetricVO getMetrics() {
        int todayCircumvention = riskResultMapper.getByLevel("medium", LocalDateTime.now().with(LocalDateTime.MIN), LocalDateTime.now());
        int blockCount = riskResultMapper.getByLevel("high", LocalDateTime.now().with(LocalDateTime.MIN), LocalDateTime.now());
        int highRiskUsers = riskResultMapper.getByLevel("high", LocalDateTime.now().with(LocalDateTime.MIN), LocalDateTime.now());
        return MetricVO.builder()
                .realtimeSessions(0)
                .todayCircumvention(todayCircumvention)
                .blockCount(blockCount)
                .highRiskUsers(highRiskUsers)
                .build();
    }
}
