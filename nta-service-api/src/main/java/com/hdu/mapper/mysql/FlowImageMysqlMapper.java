package com.hdu.mapper.mysql;

import com.hdu.entity.FlowImage;
import com.hdu.entity.ForeignAppAccessLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FlowImageMysqlMapper {
    @Select("select * from flow_image where adsl_account = #{adslAccount}")
    List<FlowImage> getFlowImagesByAdslAccount(@Param("adslAccount") String adslAccount);

    @Select("select time, client_ip, server_ip, app_name from unified_log where adsl_account = #{adslAccount} and type = 'app_act'")
    List<ForeignAppAccessLog> getForeignAppAccessLogsByAdslAccount(String adslAccount);

    List<Long> getHourlyTrendData(LocalDateTime start, LocalDateTime end);

    List<Long> getDailyTrendData(LocalDateTime start, LocalDateTime end);

    List<Long> getMonthlyTrendData(LocalDateTime start, LocalDateTime end);
}
