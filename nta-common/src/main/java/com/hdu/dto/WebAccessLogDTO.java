package com.hdu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAccessLogDTO {
    private Integer page;
    private Integer pageSize;
    private Integer uid;
    private String phoneNumber;
    private LocalDate begin;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private LocalDate end;

    public WebAccessLogDTO(String phoneNumber, LocalDateTime begin, LocalDateTime end) {
        this.phoneNumber = phoneNumber;
        this.beginTime = begin;
        this.endTime = end;
    }
}
