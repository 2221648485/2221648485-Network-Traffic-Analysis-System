package com.hdu.service;

import com.hdu.DTO.*;
import com.hdu.VO.RiskAnalysisVO;
import com.hdu.entity.*;
import com.hdu.mapper.mysql.*;
import com.hdu.rule.RiskRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregatedUserLogService {
    private final WebAccessLogMapper webAccessMapper;
    private final TunnelOfflineLogMapper tunnelOfflineLogMapper;
    private final TunnelAccessLogMapper tunnelAccessLogMapper;
    private final DeclassifyLogMapper declassifyLogMapper;
    private final ForeignAppAccessLogMapper foreignAppAccessMapper;

    private final RiskRuleEngine riskRuleEngine;

    public RiskAnalysisVO analyze(RiskQueryDTO dto) {
        String phone = dto.getPhoneNumber();
        LocalDateTime start = dto.getStartTime();
        LocalDateTime end = dto.getEndTime();

        List<WebAccessLog> webLogs = webAccessMapper.listWebAccessLog(new WebAccessLogDTO(phone, start, end));
        List<TunnelAccessLog> tunnelLogs = tunnelAccessLogMapper.listTunnelAccessLog(new TunnelAccessLogDTO(phone, start, end));
        List<ForeignAppAccessLog> appLogs = foreignAppAccessMapper.listForeignAppAccessLog(new ForeignAppAccessLogDTO(phone, start, end));
        List<DeclassifyLog> declassifyLogs = declassifyLogMapper.listDeclassifyLog(new DeclassifyLogDTO(phone, start, end));

        int score = 0;
        StringBuilder hitRules = new StringBuilder();

        score += riskRuleEngine.analyzeWebAccess(webLogs, hitRules);
        score += riskRuleEngine.analyzeTunnelAccess(tunnelLogs, hitRules);
        score += riskRuleEngine.analyzeForeignApps(appLogs, hitRules);
        score += riskRuleEngine.analyzeDeclassify(declassifyLogs, hitRules);

        String riskLevel = riskRuleEngine.calculateRiskLevel(score);

        RiskAnalysisVO result = new RiskAnalysisVO();
        result.setPhoneNumber(phone);
        result.setRiskLevel(riskLevel);
        result.setDescription(hitRules.toString());
        return result;
    }
}
