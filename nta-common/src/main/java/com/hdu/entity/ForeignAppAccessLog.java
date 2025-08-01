package com.hdu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignAppAccessLog implements Serializable {
    private final static long serialVersionUID = 1L;
    private Long id; // 主键自增

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;           // 时间

    private String phoneNumber;           // 手机号
    private String imsi;                  // IMSI
    private String imei;                  // IMEI
    private String adslAccount;           // ADSL账号
    private String clientIp;              // 客户端IP
    private String serverIp;              // 服务器IP
    private String appName;               // 境外App名称

    private Integer uid = 1;              // 批次编号（可用于分批导入时标记）

    public static ForeignAppAccessLog fromString(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return null; // 跳过空行或注释行
        }

        // 按逗号分割，-1 保证末尾空字段不丢失
        String[] parts = line.split(",", -1);

        // 校验字段数量
//        if (parts.length != 9) {
//            throw new IllegalArgumentException("字段数量不正确，期望9个字段，实际：" + parts.length);
//        }

        ForeignAppAccessLog log = new ForeignAppAccessLog();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.setTime(LocalDateTime.parse(parts[0], formatter));
        log.setPhoneNumber(parts[1]);
        log.setImsi(parts[2]);
        log.setImei(parts[3]);
        log.setAdslAccount(parts[4]);
        log.setClientIp(parts[5]);
        log.setServerIp(parts[6]);
        log.setAppName(parts[7]);

        return log;
    }

}
