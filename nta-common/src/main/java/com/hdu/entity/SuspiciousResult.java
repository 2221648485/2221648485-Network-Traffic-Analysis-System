package com.hdu.entity;

import lombok.Data;

@Data
public class SuspiciousResult {
    private String phoneNumber;
    private long count;
    private String windowStart;
    private String windowEnd;
}
