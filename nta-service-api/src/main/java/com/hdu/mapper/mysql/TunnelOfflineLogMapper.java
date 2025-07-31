package com.hdu.mapper.mysql;

import com.github.pagehelper.Page;
import com.hdu.dto.TunnelOfflineLogDTO;
import com.hdu.entity.TunnelOfflineLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TunnelOfflineLogMapper {
    @Select("SELECT COALESCE(MAX(uid), 0) FROM tunnel_offline_log")
    int getUidFromTunnelOffLine();

    int insertTunnelOfflineLogBatch(List<TunnelOfflineLog> logList);

    Page<TunnelOfflineLog> listTunnelOfflineLog(TunnelOfflineLogDTO tunnelOfflineLogDTO);
}
