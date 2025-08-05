package com.hdu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper jacksonObjectMapper() {
        return new com.hdu.json.JacksonObjectMapper();
    }
}