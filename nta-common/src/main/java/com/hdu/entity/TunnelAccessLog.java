package com.hdu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TunnelAccessLog implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer id;                 // 自增主键
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;         // 时间
    private String flowId;              // 流ID
    private String phoneNumber;         // 手机号
    private String imsi;                // IMSI
    private String imei;                // IMEI
    private String adslAccount;         // ADSL账号
    private String clientIp;            // 客户端IP
    private String serverIp;            // 服务器IP
    private String clientRegion;        // 客户端地区
    private String serverRegion;        // 服务器属地
    private String tunnelType;          // 隧道类型
    private String operator;            // 运营商
    private String tool;                // 工具
    private Integer clientPort;         // 客户端口
    private Integer serverPort;         // 服务器端口
    private Long upBytes;              // 上行包大小（单位Byte）
    private Long downBytes;            // 下行包大小（单位Byte）
    private Integer uid = 1;           // 解析批次标识（如有分页或分批需求）

    public static TunnelAccessLog fromString(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return null; // 跳过空行或注释行
        }
        // 按逗号分割，-1保证末尾空字段不丢失
        String[] parts = line.split(",", -1);
        // 该日志格式字段数为17个
        if (parts.length != 17) {
            throw new IllegalArgumentException("字段数量不正确，期望17个字段，实际：" + parts.length);
        }

        TunnelAccessLog log = new TunnelAccessLog();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setTime(LocalDateTime.parse(parts[0], formatter));
        log.setFlowId(parts[1]);
        log.setPhoneNumber(parts[2]);
        log.setImsi(parts[3]);
        log.setImei(parts[4]);
        log.setAdslAccount(parts[5]);
        log.setClientIp(parts[6]);
        log.setServerIp(parts[7]);
        log.setClientRegion(parts[8]);
        log.setServerRegion(parts[9]);
        log.setTunnelType(parts[10]);
        log.setOperator(parts[11]);
        log.setTool(parts[12]);

        // 端口和流量字段，需判空再转换
        log.setClientPort(parts[13].isEmpty() ? null : Integer.valueOf(parts[13]));
        log.setServerPort(parts[14].isEmpty() ? null : Integer.valueOf(parts[14]));
        log.setUpBytes(parts[15].isEmpty() ? null : Long.valueOf(parts[15]));
        log.setDownBytes(parts[16].isEmpty() ? null : Long.valueOf(parts[16]));
        return log;
    }
}
