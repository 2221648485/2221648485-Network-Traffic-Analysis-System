package com.hdu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TunnelOfflineLogDTO {
    private Integer page;
    private Integer pageSize;
    private Integer uid;
    private String flowId;
    private LocalDate begin;
    private LocalDateTime beginTime;
    private LocalDate end;
    private LocalDateTime endTime;
}
