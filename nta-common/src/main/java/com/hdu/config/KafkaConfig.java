package com.hdu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaConfig {
    private String bootstrapServers;
    private String username;
    private String password;
    private String groupId;
}
