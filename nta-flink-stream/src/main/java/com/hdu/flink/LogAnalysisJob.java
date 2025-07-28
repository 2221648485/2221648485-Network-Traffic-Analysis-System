package com.hdu.flink;

import com.hdu.entity.*;
import com.hdu.result.RiskResult;

import com.hdu.sink.ExternalSystemSink;
import com.hdu.sink.MysqlRiskSink;
import com.hdu.sink.RiskResultKafkaSink;
import com.hdu.utils.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.sql.Timestamp;
import java.time.Duration;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class LogAnalysisJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.getConfig().registerTypeWithKryoSerializer(LocalDateTime.class, new LocalDateTimeKryoSerializer());
        // env.setParallelism(4);

        // 1. Kafka 配置
        Properties props = new Properties();
//        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("bootstrap.servers", "kafka:9092");
        props.setProperty("group.id", "vpn-analysis-group");

        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "test",
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
                        .withTimestampAssigner((log, ts) -> Timestamp.valueOf(log.getTimeString()).getTime())
                        .withIdleness(Duration.ofSeconds(30)) // 必加，保证 watermark 推进
                );
        // 4. 风险检测与评分
        DataStream<RiskResult> riskResults = unifiedWithWatermark
                // 先让 RedisBlacklistUpdaterFunction 周期刷新黑名单缓存，并透传数据
                .flatMap(new RedisBlacklistUpdaterFunction())
                // 1. 识别所有可能的翻墙行为日志
                .filter(VPNRuleUtils::isPotentialVpnLog)
                // 2. 使用手机号作为用户标识聚合
                .keyBy(UnifiedLog::getPhoneNumber)
                // 3. 滚动 5 分钟窗口
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                // 4. 评分与模型置信度分析
                .process(new RiskScoringProcessFunction()); // 此类中已接入 Redis 和模型评分
        log.info("riskResults:{}", riskResults);
        // 5. 输出到控制台（或 Kafka / MySQL）
        riskResults.addSink(new ExternalSystemSink());
        riskResults.addSink(new MysqlRiskSink());

        // 发送到 Kafka 封禁队列
        KafkaSink<RiskResult> kafkaSink = RiskResultKafkaSink.build("kafka:9092", "ban-topic");
        riskResults.sinkTo(kafkaSink);

        env.execute("VPN & Sensitive Access Detection Job");
    }
}
