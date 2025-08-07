package com.hdu.contorller;

import com.hdu.result.Result;
import com.hdu.service.CockpitService;
import com.hdu.vo.MetricVO;
import com.hdu.vo.TrendDataVO;
import com.hdu.vo.VpnToolsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/Cockpit")
@RequiredArgsConstructor
@Tag(name = "驾驶舱相关接口")
public class CockpitController {
    private final CockpitService cockpitService;

    /**
     * 获取实时统计指标
     * @return
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取实时统计指标")
    public Result<MetricVO> getMetrics() {
        log.info("获取实时统计指标...");
        return Result.success(cockpitService.getMetrics());
    }

    @GetMapping("/vpn")
    @Operation(summary = "获取VPN指标")
    public Result<List<VpnToolsVO>> getVpnMetrics() {
        log.info("获取VPN指标...");
        return Result.success(cockpitService.getVpnMetrics());
    }

    @GetMapping("/trendData")
    @Operation(summary = "获取翻墙流量趋势图")
    public Result<TrendDataVO> getTrendData(String type, String time) {
        log.info("获取翻墙流量趋势图...");
        return Result.success(cockpitService.getTrendData(type, time));
    }

}
