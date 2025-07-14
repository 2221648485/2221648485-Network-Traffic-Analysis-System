package com.hdu.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "risk")
@Data
public class RiskProperties {
    private Map<String, Map<String, Integer>> scores;  // e.g. scores.web.sensitiveSite
    private Map<String, Integer> levels;               // e.g. levels.high

    private List<String> sensitiveSites;               // 涉及网站类型列表
    private List<String> vpnTunnelTypes;               // VPN隧道类型列表
    private List<String> riskyPorts;                    // 翻墙端口列表

    private Map<String, String> foreignApps;           // 境外App对应提示文本
}
