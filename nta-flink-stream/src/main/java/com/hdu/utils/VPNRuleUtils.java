package com.hdu.utils;

import com.hdu.entity.UnifiedLog;
import com.hdu.entity.BlacklistStore;
import java.util.Set;

public class VPNRuleUtils {
    public static boolean isInIocBlacklist(UnifiedLog log) {
        Set<String> iocIpSet = BlacklistStore.getIocServerIps();   // IOC中的恶意IP集合
        Set<String> iocHostSet = BlacklistStore.getIocHostnames(); // IOC中的恶意主机名集合

        return (log.getServerIp() != null && iocIpSet.contains(log.getServerIp()))
                || (log.getHostName() != null && iocHostSet.contains(log.getHostName()));
    }


    public static boolean isDnsForeignFailed(UnifiedLog log) {
        return "declassify_act".equals(log.getType())
                && log.getAppProtocol() != null
                && log.getAppProtocol().equalsIgnoreCase("dns")
                && log.getServerRegion() != null
                && log.getServerRegion().contains("境外")
                && log.getAppInfo() != null
                && log.getAppInfo().toLowerCase().contains("failed");
    }

    public static boolean isTlsSniVpn(UnifiedLog log) {
        return "declassify_act".equals(log.getType())
                && log.getAppProtocol() != null
                && log.getAppProtocol().equalsIgnoreCase("tls")
                && log.getHostName() != null
                && isKnownVpnSni(log.getHostName());
    }

    private static boolean isKnownVpnSni(String hostName) {
        // 典型 VPN SNI 域名关键词
        String[] vpnKeywords = {"vpn", "ssr", "shadowsocks", "lantern", "tor", "outline", "wireguard", "surfshark", "expressvpn"};
        String lower = hostName.toLowerCase();
        for (String keyword : vpnKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnectSensitivePorts(UnifiedLog log) {
        if (log.getServerPort() == null) return false;
        int port = log.getServerPort();
        return port == 1080 || port == 443 || port == 7890 || port == 8080 || port == 8888;
    }

    public static boolean isPotentialVpnLog(UnifiedLog log) {
        return true;
//        return isInIocBlacklist(log)
//                || isDnsForeignFailed(log)
//                || isTlsSniVpn(log)
//                || isConnectSensitivePorts(log);
    }
}
