package com.hdu.utils;
import com.hdu.entity.RiskRules;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigUtils {
    private static final RiskRules rules;

    static {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigUtils.class.getClassLoader().getResourceAsStream("risk-rules.yml")) {
            rules = yaml.loadAs(in, RiskRules.class);
        } catch (Exception e) {
            throw new RuntimeException("无法加载风险规则配置文件", e);
        }
    }

    public static RiskRules getRules() {
        return rules;
    }
}
