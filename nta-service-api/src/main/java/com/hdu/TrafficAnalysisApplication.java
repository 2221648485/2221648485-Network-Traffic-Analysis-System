package com.hdu;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
@MapperScan({"com.hdu.mapper.mysql", "com.hdu.mapper.sqlserver"})
public class TrafficAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficAnalysisApplication.class, args);
        log.info("服务已启动...");
    }
}
