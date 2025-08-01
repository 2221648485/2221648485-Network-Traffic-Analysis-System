package com.hdu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    private MysqlConfig mysql;
    private KafkaConfig kafka;
    private RedisConfig redis;
}

