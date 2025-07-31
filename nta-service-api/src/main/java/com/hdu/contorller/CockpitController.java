package com.hdu.contorller;

import com.hdu.result.Result;
import com.hdu.service.CockpitService;
import com.hdu.vo.MetricVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return Result.success(cockpitService.getMetrics());
    }

}
