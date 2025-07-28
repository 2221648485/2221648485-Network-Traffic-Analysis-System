package com.hdu.sink;

import com.hdu.json.JacksonObjectMapper;
import com.hdu.result.RiskResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.base.DeliveryGuarantee;

public class RiskResultKafkaSink {

    private static final ObjectMapper mapper = new JacksonObjectMapper();

    // 定义 Kafka Sink
    public static KafkaSink<RiskResult> build(String bootstrapServers, String topic) {
        return KafkaSink.<RiskResult>builder()
                .setBootstrapServers(bootstrapServers)
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(topic)
                                .setValueSerializationSchema(new RiskResultSerializationSchema())
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    // 自定义序列化器，把 RiskResult 转成 JSON bytes
    private static class RiskResultSerializationSchema implements SerializationSchema<RiskResult> {
        @Override
        public byte[] serialize(RiskResult riskResult) {
            try {
                return mapper.writeValueAsBytes(riskResult);
            } catch (Exception e) {
                throw new RuntimeException("RiskResult serialization failed", e);
            }
        }
    }
}
