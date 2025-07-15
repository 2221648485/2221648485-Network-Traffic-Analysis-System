package com.hdu.utils;

import com.hdu.entity.*;
import com.hdu.mapper.ABMapper;

public class ParseUtils {
    // 日志聚合统一方法
    public static UnifiedLog parseToUnifiedLog(String line) {
        try {
            if (line == null || line.trim().isEmpty()) return null;

            // 第一次分割：获取日志类型和剩余内容
            String[] parts = line.split(",", 2);
            if (parts.length < 2) return null;

            String type = parts[0].trim();
            String remaining = parts[1].trim();

            // 第二次分割：从剩余内容中提取时间字符串和日志主体
            String[] contentParts = remaining.split(",", 2);
            if (contentParts.length < 2) return null;
            String timeString = contentParts[0]; // 正确的时间字符串

            switch (type) {
                case "web_act":
                    WebAccessLog webLog = WebAccessLog.fromString(remaining);
                    UnifiedLog unifiedWebLog = ABMapper.INSTANCE.aToB(webLog);
                    unifiedWebLog.setType(type);
                    unifiedWebLog.setTimeString(timeString); // 设置到新增的 timeString 字段
                    return unifiedWebLog;

                case "tw_act":
                    TunnelAccessLog twLog = TunnelAccessLog.fromString(remaining);
                    UnifiedLog unifiedTwLog = ABMapper.INSTANCE.aToB(twLog);
                    unifiedTwLog.setType(type);
                    unifiedTwLog.setTimeString(timeString); // 设置到新增的 timeString 字段
                    return unifiedTwLog;

                case "app_act":
                    ForeignAppAccessLog appLog = ForeignAppAccessLog.fromString(remaining);
                    UnifiedLog unifiedAppLog = ABMapper.INSTANCE.aToB(appLog);
                    unifiedAppLog.setType(type);
                    unifiedAppLog.setTimeString(timeString); // 设置到新增的 timeString 字段
                    return unifiedAppLog;

                case "declassify_act":
                    DeclassifyLog declassifyLog = DeclassifyLog.fromString(remaining);
                    UnifiedLog unifiedDeclassifyLog = ABMapper.INSTANCE.aToB(declassifyLog);
                    unifiedDeclassifyLog.setType(type);
                    unifiedDeclassifyLog.setTimeString(timeString); // 设置到新增的 timeString 字段
                    return unifiedDeclassifyLog;

                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("日志解析失败: " + line + ", 错误: " + e.getMessage());
            return null;
        }
    }
}