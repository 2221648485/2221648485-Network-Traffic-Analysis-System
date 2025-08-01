package com.hdu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MysqlConfig {
    private String url;
    private String username;
    private String password;
}
