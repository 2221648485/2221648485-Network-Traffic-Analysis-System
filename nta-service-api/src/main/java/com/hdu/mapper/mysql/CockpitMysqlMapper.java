package com.hdu.mapper.mysql;

import com.hdu.vo.VpnToolsVO;
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

    @Select("select tools ,COUNT(*) as count from flow_image where tools != '' and tools != '未知' GROUP BY tools\n" +
            "ORDER BY count DESC")
    List<VpnToolsVO> getVpnInfo();
}
