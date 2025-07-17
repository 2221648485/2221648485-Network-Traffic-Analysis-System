package com.hdu.mapper;

import com.hdu.result.RiskResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiskResultMapper {
    List<RiskResult> selectRecentRiskResults(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
