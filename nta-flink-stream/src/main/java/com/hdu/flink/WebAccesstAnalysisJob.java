package com.hdu.flink;

import com.esotericsoftware.kryo.serializers.TimeSerializers;
import com.hdu.entity.WebAccessLog;
import lombok.RequiredArgsConstructor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.LocalDateTime;
import java.util.Objects;

// VM option 配置
// --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
@RequiredArgsConstructor
public class WebAccesstAnalysisJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().registerTypeWithKryoSerializer(
                LocalDateTime.class,  // 目标类型：LocalDateTime
                TimeSerializers.LocalDateTimeSerializer.class  // 专用序列化器
        );

        // 本地测试文件路径
        DataStream<String> raw = env.readTextFile("C:\\Users\\86188\\Desktop\\网络流量分析系统\\日志模板\\web_act_20230817103926180.txt");

        raw.map(WebAccessLog::fromString)
                .filter(Objects::nonNull)  // 过滤掉null对象，防止空指针
                .filter(log -> log.getSiteType().contains("涉藏")
                        || log.getTunnelType().toLowerCase().contains("ssl")
                        || log.getTool().toLowerCase().contains("vpn"))
                .map(log -> {
                    String reason = String.format("命中规则: %s | %s | %s",
                            log.getSiteType(), log.getTunnelType(), log.getTool());
//                     RedisUtil.writeFlag(log.getPhoneNumber(), reason);
                    return "[告警] " + log.getPhoneNumber() + " → " + reason;
                })
                .print();

        env.execute("Flink WebAct Analysis");
    }
}
