package com.hdu.mapper.mysql;

import com.github.pagehelper.Page;
import com.hdu.DTO.TunnelAccessLogDTO;
import com.hdu.entity.TunnelAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TunnelAccessLogMapper {
    @Select("SELECT COALESCE(MAX(uid), 0) FROM tunnel_access_log")
    int getUidFromTunnelAccess();

    int insertTunnelAccessLogBatch(List<TunnelAccessLog> logList);

    Page<TunnelAccessLog> listTunnelAccessLog(TunnelAccessLogDTO tunnelAccessLogDTO);
}
