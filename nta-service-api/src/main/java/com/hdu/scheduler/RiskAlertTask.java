package com.hdu.scheduler;


import com.hdu.mapper.RiskResultMapper;
import com.hdu.result.RiskResult;
import com.hdu.service.UserActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RiskAlertTask {

    private final RiskResultMapper riskResultMapper;
    private final UserActionService userActionService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void processRecentRiskResults() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(6);

        List<RiskResult> riskResults = riskResultMapper.selectRecentRiskResults(startTime, endTime);

        for (RiskResult r : riskResults) {
            try {
                log.info("处理风险用户: {} 风险等级: {}", r.getPhoneNumber(), r.getRiskLevel());
                userActionService.triggerPaAction(r.getPhoneNumber(), r.getRiskLevel()); // 调用 PA 接口 (高风险下线)
                userActionService.applyAaaPolicy(r.getPhoneNumber(), r.getRiskLevel()); // 调用 AAA 接口 (删除mac地址)
                userActionService.moveUserToRiskGroup(r.getPhoneNumber(), r.getRiskLevel()); // 调用 PA 接口 (转移用户组)
                // 标记或其他处理逻辑（如状态字段），如果有设计的话
            } catch (Exception e) {
                log.error("处理风险用户失败: {}", r.getPhoneNumber(), e);
            }
        }
    }
}