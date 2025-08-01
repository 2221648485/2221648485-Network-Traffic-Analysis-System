package com.hdu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPortrait implements Serializable {
    private String flowId;
    private String phoneNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalUpBytes;
    private long totalDownBytes;
    private Set<String> siteNames = new HashSet<>();
    private Set<String> tools = new HashSet<>();
    private String tunnelType;
    private long totalBytes;
    private LocalDateTime createTime;

    @Override
    public String toString() {
        return "\n【用户画像】\n" +
                "流水ID       ：" + flowId + "\n" +
                "手机号       ：" + phoneNumber + "\n" +
                "会话开始时间 ：" + startTime + "\n" +
                "会话结束时间 ：" + endTime + "\n" +
                "上行流量总计 ：" + totalUpBytes + " 字节\n" +
                "下行流量总计 ：" + totalDownBytes + " 字节\n" +
                "隧道类型     ：" + tunnelType + "\n" +
                "访问网站列表 ：" + String.join("，", siteNames) + "\n" +
                "使用工具列表 ：" + String.join("，", tools) + "\n" +
                "使用总计流量 : " + totalBytes + " 字节\n";
    }

}
