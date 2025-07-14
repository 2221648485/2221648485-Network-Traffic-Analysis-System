package com.hdu.utils;

import com.hdu.entity.*;
import com.hdu.mapper.ABMapper;

public class ParseUtils {
    // 日志聚合统一方法
    public static UnifiedLog parseToUnifiedLog(String line) {
        try {
            if (line == null || line.trim().isEmpty()) return null;

            String[] parts = line.split(",", 2);
            String type = parts[0].trim();
            switch (type) {
                case "web_act":
                    return ABMapper.INSTANCE.aToB(WebAccessLog.fromString(parts[1]));
                case "tw_act":
                    return ABMapper.INSTANCE.aToB(TunnelAccessLog.fromString(parts[1]));
                case "tw_act_off":
                    return ABMapper.INSTANCE.aToB(TunnelOfflineLog.fromString(parts[1]));
                case "app_act":
                    return ABMapper.INSTANCE.aToB(ForeignAppAccessLog.fromString(parts[1]));
                case "declassify_act":
                    return ABMapper.INSTANCE.aToB(DeclassifyLog.fromString(parts[1]));
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
