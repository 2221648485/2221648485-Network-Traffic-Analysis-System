package com.hdu.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {
    private String phoneNumber;
    private String riskLevel;
    private double riskScore;
    private double modelConfidence;
    private long windowStart;
    private long windowEnd;
}
