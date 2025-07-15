package com.hdu.flink;

import com.hdu.entity.*;
import com.hdu.result.RiskResult;

import com.hdu.sink.ExternalSystemSink;
import com.hdu.utils.RiskScoringProcessFunction;

import com.hdu.utils.VPNRuleUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import com.hdu.utils.ParseUtils;

import java.sql.Timestamp;
import java.time.Duration;

import java.util.Objects;
import java.util.Properties;

public class LogAnalysisJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // env.setParallelism(4);

        // 1. Kafka 配置
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "vpn-analysis-group");

        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "raw_logs",
                new SimpleStringSchema(),
                props
        );
        kafkaSource.setStartFromLatest();

        // 2. 从 Kafka 中读取并统一为 UnifiedLog
        DataStream<UnifiedLog> unifiedStream = env
                .addSource(kafkaSource)
                .map(ParseUtils::parseToUnifiedLog)
                .filter(Objects::nonNull);

        // 3. 时间戳与水位线
        DataStream<UnifiedLog> unifiedWithWatermark = unifiedStream
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<UnifiedLog>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                        .withTimestampAssigner((log, ts) -> Timestamp.valueOf(log.getTime()).getTime())
                );

        // 4. 风险检测与评分
        DataStream<RiskResult> riskResults = unifiedWithWatermark
                // 1. 识别所有可能的翻墙行为日志
                .filter(VPNRuleUtils::isPotentialVpnLog
                )
                // 2. 使用手机号作为用户标识聚合
                .keyBy(UnifiedLog::getPhoneNumber)
                // 3. 滚动 5 分钟窗口
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                // 4. 评分与模型置信度分析
                .process(new RiskScoringProcessFunction()); // 此类中已接入 Redis 和模型评分

        // 5. 输出到控制台（或 Kafka / MySQL）
        riskResults.print();
        riskResults.addSink(new ExternalSystemSink());

        env.execute("VPN & Sensitive Access Detection Job");
    }
}
