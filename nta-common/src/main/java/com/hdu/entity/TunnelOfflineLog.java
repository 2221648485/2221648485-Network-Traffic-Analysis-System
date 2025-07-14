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
public class TunnelOfflineLog implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer id;                 // 自增主键
    private String flowId;              // 流ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime offlineTime; // 下线时间

    private Long totalBytes;            // 流总大小（单位 Byte）
    private Integer uid = 1;

    public static TunnelOfflineLog fromString(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return null; // 跳过空行或注释行
        }

        // 使用 -1 保证末尾空字段不丢失
        String[] parts = line.split(",", -1);

        // 检查字段数是否为3
        if (parts.length != 3) {
            throw new IllegalArgumentException("字段数量不正确，期望3个字段，实际：" + parts.length);
        }

        TunnelOfflineLog log = new TunnelOfflineLog();
        log.setFlowId(parts[0]);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setOfflineTime(LocalDateTime.parse(parts[1], formatter));

        log.setTotalBytes(parts[2].isEmpty() ? null : Long.valueOf(parts[2]));

        return log;
    }
}
