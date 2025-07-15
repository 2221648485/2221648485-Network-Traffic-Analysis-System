package com.hdu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UnifiedLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 公共字段（几乎所有日志都有） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;        // 时间（上线、访问等）
    private String timeString;         // 时间字符串（用于解析）
    private String type;               // 日志类型，如 web_act、tw_act、app_act 等
    private String phoneNumber;        // 手机号
    private String imsi;               // IMSI
    private String imei;               // IMEI
    private String adslAccount;        // ADSL账号
    private String clientIp;           // 客户端IP
    private String serverIp;           // 服务器IP
    private String clientRegion;       // 客户端地区
    private String serverRegion;       // 服务器地区
    private String tunnelType;         // 隧道类型
    private String operator;           // 运营商
    private String tool;               // 工具（如Shadowsocks）

    /** 网站访问相关（web_act） */
    private String siteName;           // 网站名称
    private String siteUrl;            // 网站URL
    private String siteType;           // 网站类型（涉恐/涉政等）
    private Integer clientPort;        // 客户端端口
    private Integer serverPort;        // 服务器端口
    private Long upBytes;              // 上行包大小
    private Long downBytes;            // 下行包大小
    private String credibility;        // 可信度（字符串或小数）

    /** 流量日志相关字段（tw_act、tw_act_off、declassify_act） */
    private String flowId;             // 流ID
    private Integer packetIndex;       // 包号
    private String originalFileName;   // 原始文件名

    /** 下线日志相关（tw_act_off） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime offlineTime; // 下线时间
    private Long totalBytes;           // 流总大小（单位 Byte）

    /** 境外 App 使用日志（app_act） */
    private String appName;            // 境外 app 名称（如 Instagram）

    /** 解密行为日志（declassify_act） */
    private String networkProtocol;    // 网络协议（如 L2TP）
    private String appProtocol;        // 应用层协议（如 tftp）
    private String appInfo;            // 应用层信息（如 Instagram）
    private String hostName;           // 主机名信息

    /** 分批导入相关（可选） */
    private Integer uid = 1;           // 批次编号

    /** 原始行（可选） */
    private String rawLine;            // Kafka 消息原始内容


    public UnifiedLog setType(String type) {
        this.type = type;
        return this;
    }
}
