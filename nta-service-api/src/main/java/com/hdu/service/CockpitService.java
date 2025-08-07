package com.hdu.service;

import com.hdu.mapper.mysql.CockpitMysqlMapper;
import com.hdu.mapper.mysql.FlowImageMysqlMapper;
import com.hdu.mapper.mysql.RiskResultMapper;
import com.hdu.vo.MetricVO;
import com.hdu.vo.TrendDataVO;
import com.hdu.vo.VpnToolsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CockpitService {
    private final CockpitMysqlMapper cockpitMysqlMapper;
    private final RiskResultMapper riskResultMapper;
    private final FlowImageMysqlMapper flowImageMysqlMapper;

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

    public List<VpnToolsVO> getVpnMetrics() {
        return cockpitMysqlMapper.getVpnInfo();
    }

    public TrendDataVO getTrendData(String type, String time) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        TrendDataVO trendDataVO = new TrendDataVO();
        switch (type) {
            case "day":
                LocalDate day = LocalDate.parse(time);
                start = day.atStartOfDay();
                end = start.plusDays(1);
                trendDataVO.setXAxis(generateHourXAxis());
                trendDataVO.setTrendData(flowImageMysqlMapper.getHourlyTrendData(start, end));
                break;
            case "month":
                YearMonth month = YearMonth.parse(time);
                start = month.atDay(1).atStartOfDay();
                end = start.plusMonths(1);
                trendDataVO.setXAxis(generateDayXAxis(month));
                trendDataVO.setTrendData(flowImageMysqlMapper.getDailyTrendData(start, end));
                break;
            case "year":
                int year = Integer.parseInt(time);
                start = LocalDate.of(year, 1, 1).atStartOfDay();
                end = start.plusYears(1);
                trendDataVO.setXAxis(generateMonthXAxis());
                trendDataVO.setTrendData(flowImageMysqlMapper.getMonthlyTrendData(start, end));
        }
        return trendDataVO;
    }


    private List<String> generateHourXAxis() {
        return IntStream.range(0, 24)
                .mapToObj(i -> String.format("%02d:00", i))
                .collect(Collectors.toList());
    }

    private List<String> generateDayXAxis(YearMonth month) {
        int daysInMonth = month.lengthOfMonth();
        return IntStream.rangeClosed(1, daysInMonth)
                .mapToObj(i -> String.format("%02d日", i))
                .collect(Collectors.toList());
    }

    private List<String> generateMonthXAxis() {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> String.format("%02d月", i))
                .collect(Collectors.toList());
    }
}
