package com.hdu.config;

import lombok.Data;

import java.util.List;

@Data
public class RiskRules {
    private List<String> sensitiveSiteTypes;
    private List<String> vpnSni;
    private List<Integer> sensitivePorts;
    private List<String> dnsFailedKeywords;
}
