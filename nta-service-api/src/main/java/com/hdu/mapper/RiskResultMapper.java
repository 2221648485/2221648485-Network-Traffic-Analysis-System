package com.hdu.mapper;

import com.hdu.result.RiskResult;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiskResultMapper {
    List<RiskResult> selectRecentNEWRiskResults(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    void update(RiskResult r);
}
