package com.hdu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataVO {
    private List<String> xAxis;     // 横坐标（hour/day/month）
    private List<Long> trendData;   // 对应流量数据
}
