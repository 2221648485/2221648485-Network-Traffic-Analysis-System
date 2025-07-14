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
public class WebAccessLog implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer id;             // 自增主键
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;     // 时间
    private String phoneNumber;     // 手机号
    private String imsi;            // IMSI
    private String imei;            // IMEI
    private String adslAccount;     // adsl账号
    private String siteName;        // 访问网站名称
    private String siteUrl;         // 访问网站网址
    private String siteType;        // 网站类型
    private String clientIp;        // 客户端IP
    private String serverIp;        // 服务器IP
    private String clientRegion;    // 客户端地区
    private String serverRegion;    // 服务器地区
    private String tunnelType;      // 隧道类型
    private String operator;        // 运营商
    private String tool;            // 工具
    private Integer clientPort;     // 客户端口
    private Integer serverPort;     // 服务器端口
    private Long upBytes;           // 上行包大小，单位Byte
    private Long downBytes;         // 下行包大小，单位Byte
    private String credibility;     // 可信度，假设是小数，如果是整数可改为Integer
    private Integer uid = 1;        // 次序
    public static WebAccessLog fromString(String line) {
        // 检查行是否为空或空白
        if (line.isEmpty() || line.startsWith("#")) {
            return null; // 跳过空行或注释行
        }
        // 以逗号分隔字段
        String[] parts = line.split(",", -1);
        if (parts.length != 20) {
            // 字段数不对，返回null或抛异常
            throw new IllegalArgumentException("字段数量不正确，期望20个字段，实际：" + parts.length);
        }
        for (int i = 0; i < parts.length; i++) {
            System.out.println(parts[i]);
        }
        WebAccessLog log = new WebAccessLog();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setTime(LocalDateTime.parse(parts[0], formatter));
        log.setPhoneNumber(parts[1]);
        log.setImsi(parts[2]);
        log.setImei(parts[3]);
        log.setAdslAccount(parts[4]);
        log.setSiteName(parts[5]);
        log.setSiteUrl(parts[6]);
        log.setSiteType(parts[7]);
        log.setClientIp(parts[8]);
        log.setServerIp(parts[9]);
        log.setClientRegion(parts[10]);
        log.setServerRegion(parts[11]);
        log.setTunnelType(parts[12]);
        log.setOperator(parts[13]);
        log.setTool(parts[14]);
        log.setClientPort(Integer.valueOf(parts[15]));
        log.setServerPort(Integer.valueOf(parts[16]));
        log.setUpBytes(Long.valueOf(parts[17]));
        log.setDownBytes(Long.valueOf(parts[18]));
        log.setCredibility(parts[19]);
        return log;
    }
}
