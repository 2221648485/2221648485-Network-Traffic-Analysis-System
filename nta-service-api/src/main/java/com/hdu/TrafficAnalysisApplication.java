package com.hdu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TrafficAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficAnalysisApplication.class, args);
        log.info("服务已启动...");
    }

}
