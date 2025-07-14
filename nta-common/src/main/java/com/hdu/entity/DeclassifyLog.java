package com.hdu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeclassifyLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;       // 时间

    private String flowId;            // 流ID
    private String phoneNumber;       // 手机号
    private String imsi;              // IMSI
    private String imei;              // IMEI
    private String adslAccount;       // ADSL账号
    private String clientIp;          // 客户端IP
    private Integer clientPort;       // 客户端端口
    private String serverIp;          // 服务器IP
    private Integer serverPort;       // 服务器端口
    private String clientRegion;      // 客户端地区
    private String serverRegion;      // 服务器地区
    private String originalFileName;  // 原始文件名
    private Integer packetIndex;      // 包号
    private String networkProtocol;   // 网络协议（如：L2TP）
    private String appProtocol;       // 应用层协议（如：tftp）
    private String appInfo;           // 应用层信息（如：Instagram）
    private String hostName;          // 主机名信息

    private Integer uid = 1;          // 批次号（用于分批导入标识）

    public static DeclassifyLog fromString(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return null; // 跳过空行或注释行
        }

        // 按逗号分割，-1 保证空字段不丢失
        String[] parts = line.split(",", -1);

        // 应该有 18 个字段
        if (parts.length != 18) {
            throw new IllegalArgumentException("字段数量不正确，期望18个字段，实际：" + parts.length);
        }

        DeclassifyLog log = new DeclassifyLog();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.setTime(LocalDateTime.parse(parts[0], formatter));
        log.setFlowId(parts[1]);
        log.setPhoneNumber(parts[2]);
        log.setImsi(parts[3]);
        log.setImei(parts[4]);
        log.setAdslAccount(parts[5]);
        log.setClientIp(parts[6]);
        log.setClientPort(parts[7].isEmpty() ? null : Integer.valueOf(parts[7]));
        log.setServerIp(parts[8]);
        log.setServerPort(parts[9].isEmpty() ? null : Integer.valueOf(parts[9]));
        log.setClientRegion(parts[10]);
        log.setServerRegion(parts[11]);
        log.setOriginalFileName(parts[12]);
        log.setPacketIndex(parts[13].isEmpty() ? null : Integer.valueOf(parts[13]));
        log.setNetworkProtocol(parts[14]);
        log.setAppProtocol(parts[15]);
        log.setAppInfo(parts[16]);
        log.setHostName(parts[17]);

        return log;
    }

}
