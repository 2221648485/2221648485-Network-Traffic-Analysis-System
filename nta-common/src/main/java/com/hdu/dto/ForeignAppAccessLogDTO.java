package com.hdu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignAppAccessLogDTO {
    private Integer page;
    private Integer pageSize;
    private Integer uid;
    private String phoneNumber;
    private LocalDateTime beginTime;
    private LocalDate begin;
    private LocalDateTime endTime;
    private LocalDate end;

    public ForeignAppAccessLogDTO(String phoneNumber, LocalDateTime beginTime, LocalDateTime endTime) {
        this.phoneNumber = phoneNumber;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
}
