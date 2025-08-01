package com.hdu.flink;

import com.hdu.config.KafkaConfig;
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
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class LogAnalysisJob {

    private static final KafkaConfig kafkaConfig = ConfigUtils.getKafkaConfig();
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(6);
        env.setStateBackend(new EmbeddedRocksDBStateBackend(true));

//        // 设置 checkpoint
//        env.enableCheckpointing(300000); // 5分钟
//        env.getCheckpointConfig().setCheckpointStorage(path);
//        env.getCheckpointConfig().setCheckpointTimeout(60000);

        // 1. Kafka 配置
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaConfig.getBootstrapServers());
        props.setProperty("group.id", kafkaConfig.getGroupId());

        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "lingyu-log",
                new SimpleStringSchema(),
                props
        );

        // 2. 从 Kafka 中读取并统一为 UnifiedLog
        DataStream<UnifiedLog> unifiedStream = env
                .addSource(kafkaSource)
                .map(ParseUtils::parseToUnifiedLog)
                .filter(Objects::nonNull);
        unifiedStream.print();
        // 3. 时间戳与水位线
        DataStream<UnifiedLog> unifiedWithWatermark = unifiedStream
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<UnifiedLog>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                        .withTimestampAssigner((log, ts) -> Timestamp.valueOf(log.getTimeString()).getTime())
                        .withIdleness(Duration.ofSeconds(30)) // 必加，保证 watermark 推进
                );
        // 4. 风险检测
        DataStream<RiskResult> riskResults = unifiedWithWatermark
                // 先让 RedisBlacklistUpdaterFunction 周期刷新黑名单缓存，并透传数据
//                .flatMap(new RedisBlacklistUpdaterFunction())
                // 1. 识别所有可能的翻墙行为日志
                .filter(VPNRuleUtils::isPotentialVpnLog)
                // 2. 使用adsl账号作为用户标识聚合
                .keyBy(UnifiedLog::getAdslAccount)
                // 3. 滚动 5 分钟窗口
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                // 4. 评分与模型置信度分析
                .process(new RiskScoringProcessFunction()); // 此类中已接入 Redis 和模型评分

        log.info("riskResults:{}", riskResults);
        // 5. 输出到控制台（或 Kafka / MySQL）
//        riskResults.addSink(new ExternalSystemSink());
        riskResults.addSink(new MysqlRiskSink());

        // 发送到 Kafka 封禁队列
        KafkaSink<RiskResult> kafkaSink = RiskResultKafkaSink.build(kafkaConfig.getBootstrapServers(), "ban-topic");
        riskResults.sinkTo(kafkaSink);

        env.execute("VPN & Sensitive Access Detection Job");
    }
}
