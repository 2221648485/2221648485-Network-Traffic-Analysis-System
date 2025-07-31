package com.hdu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclassifyLogDTO {
    private Integer page;
    private Integer pageSize;
    private Integer uid;
    private String flowId;
    private String phoneNumber;
    private LocalDate begin;
    private LocalDateTime beginTime;
    private LocalDate end;
    private LocalDateTime endTime;

    public DeclassifyLogDTO(String phoneNumber, LocalDateTime beginTime, LocalDateTime endTime) {
        this.phoneNumber = phoneNumber;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }


}
