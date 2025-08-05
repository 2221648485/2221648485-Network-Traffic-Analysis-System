package com.hdu.utils;

import com.hdu.entity.UnifiedLog;
import com.hdu.entity.FlowImage;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class FlowImageWindowFunction extends ProcessWindowFunction<
        UnifiedLog, FlowImage, String, TimeWindow> {

    @Override
    public void process(String flowId,
                        Context context,
                        Iterable<UnifiedLog> logs,
                        Collector<FlowImage> out) {
        int i = 0;
        String adslAccount = "";
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
            if (log.getType().equals("tw_act")) {
                adslAccount = log.getAdslAccount();
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
        FlowImage u = new FlowImage(flowId, adslAccount, startTime, endTime,
                totalUp, totalDown, sites, tools, tunnelType, totalBytes, null);
        out.collect(u);
    }

    private static boolean notEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
