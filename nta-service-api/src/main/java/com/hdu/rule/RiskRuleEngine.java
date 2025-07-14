package com.hdu.rule;

import com.hdu.properties.RiskProperties;
import com.hdu.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RiskRuleEngine {

    private final RiskProperties riskProperties;

    public int analyzeWebAccess(List<WebAccessLog> logs, StringBuilder hitRules) {
        int score = 0;
        List<String> sensitiveSites = riskProperties.getSensitiveSites();
        int sensitiveSiteScore = riskProperties.getScores().getOrDefault("web", Map.of()).getOrDefault("sensitiveSite", 3);
        int vpnToolScore = riskProperties.getScores().getOrDefault("web", Map.of()).getOrDefault("vpnTool", 2);

        for (WebAccessLog log : logs) {
            if (log.getSiteType() != null && sensitiveSites.contains(log.getSiteType())) {
                score += sensitiveSiteScore;
                hitRules.append("访问涉密网站[").append(log.getSiteType()).append("]；");
            }
            if (log.getTool() != null && log.getTool().toLowerCase().contains("vpn")) {
                score += vpnToolScore;
                hitRules.append("使用VPN工具；");
            }
        }
        return score;
    }

    public int analyzeTunnelAccess(List<TunnelAccessLog> logs, StringBuilder hitRules) {
        int score = 0;
        List<String> riskyPorts = riskProperties.getRiskyPorts();
        int riskyPortScore = riskProperties.getScores().getOrDefault("tunnel", Map.of()).getOrDefault("riskyPort", 2);
        List<String> vpnTunnelTypes = riskProperties.getVpnTunnelTypes();

        for (TunnelAccessLog log : logs) {
            if (log.getServerPort() != null && riskyPorts.contains(String.valueOf(log.getServerPort()))) {
                score += riskyPortScore;
                hitRules.append("访问翻墙典型端口[").append(log.getServerPort()).append("]；");
            }
            if (log.getTunnelType() != null && vpnTunnelTypes.stream()
                    .anyMatch(vpnType -> vpnType.equalsIgnoreCase(log.getTunnelType()))) {
                score += riskyPortScore; // 或者你可以单独给隧道类型一个分值配置
                hitRules.append("使用VPN隧道类型[").append(log.getTunnelType()).append("]；");
            }
        }
        return score;
    }

    public int analyzeForeignApps(List<ForeignAppAccessLog> logs, StringBuilder hitRules) {
        int score = 0;
        Map<String, Integer> appScores = riskProperties.getScores().getOrDefault("foreignApp", Map.of());
        Map<String, String> foreignApps = riskProperties.getForeignApps();

        for (ForeignAppAccessLog log : logs) {
            String name = log.getAppName();
            if (name != null) {
                for (String appName : foreignApps.keySet()) {
                    if (appName.equalsIgnoreCase(name)) {
                        score += appScores.getOrDefault(appName, 2);
                        hitRules.append(foreignApps.get(appName));
                        break;
                    }
                }
            }
        }
        return score;
    }

    public int analyzeDeclassify(List<DeclassifyLog> logs, StringBuilder hitRules) {
        int score = 0;
        int tunnelKeywordScore = riskProperties.getScores().getOrDefault("declassify", Map.of()).getOrDefault("tunnelKeyword", 3);

        for (DeclassifyLog log : logs) {
            if (log.getAppInfo() != null && log.getAppInfo().toLowerCase().contains("tunnel")) {
                score += tunnelKeywordScore;
                hitRules.append("应用层识别翻墙行为；");
            }
        }
        return score;
    }

    public String calculateRiskLevel(int score) {
        int high = riskProperties.getLevels().getOrDefault("high", 6);
        int medium = riskProperties.getLevels().getOrDefault("medium", 3);

        if (score >= high) return "HIGH";
        if (score >= medium) return "MEDIUM";
        return "LOW";
    }
}
