package com.hdu.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAnalysisVO {
    private String phoneNumber;
    private String riskLevel;      // LOW / MEDIUM / HIGH
    private String description;    // 命中规则描述
}
