package com.hdu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdu.json.JacksonObjectMapper;
import com.hdu.result.RiskResult;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final ObjectMapper jacksonObjectMapper;
    @Bean
    public ConsumerFactory<String, RiskResult> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.249.46.48:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ban-topic");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // JsonDeserializer 反序列化 RiskResult
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.hdu.result");

        // 1. 创建自定义 JsonDeserializer，关联你的 JacksonObjectMapper
        JsonDeserializer<RiskResult> jsonDeserializer = new JsonDeserializer<>(
                RiskResult.class,
                jacksonObjectMapper,
                false
        );

        // 2. 用 ErrorHandlingDeserializer 包装，增强错误处理
        ErrorHandlingDeserializer<RiskResult> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        // 3. 构造消费者工厂，使用带错误处理的反序列化器
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),  // key 反序列化器
                errorHandlingDeserializer  // value 反序列化器（带错误处理）
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiskResult> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RiskResult> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
