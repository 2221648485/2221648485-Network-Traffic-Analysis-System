package com.hdu.utils;

import com.hdu.entity.RiskRules;
import com.hdu.entity.UnifiedLog;
import com.hdu.entity.BlacklistStore;

import java.util.Set;

public class VPNRuleUtils {

    private static final RiskRules rules = ConfigUtils.getRules();

    public static boolean isInIocBlacklist(UnifiedLog log) {
        if (log == null) return false;
        Set<String> iocIpSet = BlacklistStore.getIocServerIps();
        Set<String> iocHostSet = BlacklistStore.getIocHostnames();
        return (notEmpty(log.getServerIp()) && iocIpSet.contains(log.getServerIp()))
                || (notEmpty(log.getHostName()) && iocHostSet.contains(log.getHostName()));
    }

    public static boolean isDnsForeignFailed(UnifiedLog log) {
        if (log == null) return false;
        return typeEquals(log, "declassify_act")
                && stringEqualsIgnoreCase(log.getAppProtocol(), "dns")
                && contains(log.getServerRegion(), "境外")
                && containsAnyKeyword(safeLower(log.getAppInfo()), rules.getDnsFailedKeywords());
    }

    public static boolean isTlsSniVpn(UnifiedLog log) {
        if (log == null) return false;
        return typeEquals(log, "declassify_act")
                && stringEqualsIgnoreCase(log.getAppProtocol(), "tls")
                && notEmpty(log.getHostName())
                && containsAnyKeyword(log.getHostName().toLowerCase(), rules.getVpnSni());
    }

    public static boolean isConnectSensitivePorts(UnifiedLog log) {
        if (log == null || log.getServerPort() == null) return false;
        return rules.getSensitivePorts().contains(log.getServerPort());
    }

    public static boolean isSensitiveContentAccess(UnifiedLog log) {
        if (log == null || log.getSiteType() == null) return false;
        return containsAnyKeyword(log.getSiteType(), rules.getSensitiveSiteTypes());
    }

    public static boolean isPotentialVpnLog(UnifiedLog log) {
        return isInIocBlacklist(log)
                || isDnsForeignFailed(log)
                || isTlsSniVpn(log)
                || isConnectSensitivePorts(log)
                || isSensitiveContentAccess(log);
    }

    // ---------- 公共工具方法 ----------

    private static boolean containsAnyKeyword(String content, Iterable<String> keywords) {
        if (content == null || keywords == null) return false;
        for (String keyword : keywords) {
            if (content.contains(keyword)) return true;
        }
        return false;
    }

    private static boolean contains(String container, String keyword) {
        return notEmpty(container) && notEmpty(keyword) && container.contains(keyword);
    }

    private static boolean notEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static boolean stringEqualsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private static boolean typeEquals(UnifiedLog log, String expectedType) {
        return log.getType() != null && log.getType().equals(expectedType);
    }

    private static String safeLower(String str) {
        return str == null ? "" : str.toLowerCase();
    }
}
