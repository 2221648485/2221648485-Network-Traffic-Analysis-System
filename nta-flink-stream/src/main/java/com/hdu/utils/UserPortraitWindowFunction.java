package com.hdu.utils;

import com.hdu.entity.UnifiedLog;
import com.hdu.vo.UserPortrait;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import javax.naming.Context;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UserPortraitWindowFunction extends ProcessWindowFunction<
        UnifiedLog, UserPortrait, String, TimeWindow> {

    @Override
    public void process(String flowId,
                        Context context,
                        Iterable<UnifiedLog> logs,
                        Collector<UserPortrait> out) {
        int i = 0;
        String phoneNumber = "";
        long totalUp = 0, totalDown = 0;
        Set<String> sites = new HashSet<>();
        Set<String> tools = new HashSet<>();
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        String tunnelType = null;
        long totalBytes = 0L;

        for (UnifiedLog log : logs) {
            i ++;
            // 取手机号优先非空
            if (phoneNumber == "" || log.getPhoneNumber() != "") {
                phoneNumber = log.getPhoneNumber();
            }

            // 上线时间取最早的time字段
            if (log.getTime() != null) {
                if (startTime == null || log.getTime().isBefore(startTime)) {
                    startTime = log.getTime();
                }
            }

            // 下线时间取最晚的offlineTime字段
            if (log.getOfflineTime() != null) {
                if (endTime == null || log.getOfflineTime().isAfter(endTime)) {
                    endTime = log.getOfflineTime();
                }
            }

            // 统计流量，上行、下行流量累加
            totalUp += (log.getUpBytes() != null ? log.getUpBytes() : 0);
            totalDown += (log.getDownBytes() != null ? log.getDownBytes() : 0);

            // 下线日志中的流总大小
            if (log.getTotalBytes() != null) {
                totalBytes += log.getTotalBytes();
            }

            if (log.getSiteName() != null) sites.add(log.getSiteName());
            if (log.getTool() != null) tools.add(log.getTool());

            if (tunnelType == null && log.getTunnelType() != null) {
                tunnelType = log.getTunnelType();
            }
        }
        UserPortrait u = new UserPortrait(flowId, phoneNumber, startTime, endTime,
                totalUp, totalDown, sites, tools, tunnelType, totalBytes, null);
        out.collect(u);
    }
}
