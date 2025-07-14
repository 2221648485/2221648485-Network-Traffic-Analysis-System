package com.hdu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskQueryDTO {
    private String phoneNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
