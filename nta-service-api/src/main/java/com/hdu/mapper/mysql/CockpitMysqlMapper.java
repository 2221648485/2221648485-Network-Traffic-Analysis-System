package com.hdu.mapper.mysql;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CockpitMysqlMapper {
    int getRealtimeSessions(@Param("begin") LocalDateTime begin);

    @Select("select DISTINCT tools ,COUNT(*) as count from user_portrait where tools != '' and tools != '未知' GROUP BY tools\n" +
            "ORDER BY count DESC")
    Map<String, Integer> getVpnInfo();
}
