package com.hdu.utils;
import com.hdu.config.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigUtils {
    private static final RiskRules rules;
    private static final Config config;
    private static final String configFilePath = "config-dev.yml";
    static {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream("risk-rules.yml")) {
            rules = yaml.loadAs(in, RiskRules.class);
        } catch (Exception e) {
            throw new RuntimeException("无法加载风险规则配置文件", e);
        }
    }

    static {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream(configFilePath)) {
            if (in == null) {
                throw new RuntimeException(configFilePath + " 配置文件未找到");
            }
            config = yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("无法加载配置文件" + configFilePath, e);
        }
    }

    public static RiskRules getRules() {
        return rules;
    }

    public static MysqlConfig getMysqlConfig() {
        return config.getMysql();
    }

    public static KafkaConfig getKafkaConfig() {
        return config.getKafka();
    }

    public static RedisConfig getRedisConfig() {
        return config.getRedis();
    }
}
