package com.hdu.consumer;

import com.hdu.mapper.RiskResultMapper;
import com.hdu.result.RiskResult;
import com.hdu.service.UserActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskResultConsumer {

    private final UserActionService userActionService;
    private final RiskResultMapper riskResultMapper;
    @KafkaListener(topics = "ban-topic", groupId = "risk-result-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(RiskResult r) {
        log.info("收到Kafka封禁消息: {}", r);
        try {
            log.info("处理风险用户: {} 风险等级: {}", r.getPhoneNumber(), r.getRiskLevel());
            userActionService.triggerPaAction(r.getPhoneNumber(), r.getRiskLevel()); // 调用 PA 接口 (高风险下线)
            userActionService.applyAaaPolicy(r.getPhoneNumber(), r.getRiskLevel()); // 调用 AAA 接口 (删除mac地址)
            userActionService.moveUserToRiskGroup(r.getPhoneNumber(), r.getRiskLevel()); // 调用 PA 接口 (转移用户组)
            r.setStatus("DONE");
            riskResultMapper.update(r); // 更新
        } catch (Exception e) {
            log.error("处理风险用户失败: {}", r.getPhoneNumber(), e);
        }

    }
}
