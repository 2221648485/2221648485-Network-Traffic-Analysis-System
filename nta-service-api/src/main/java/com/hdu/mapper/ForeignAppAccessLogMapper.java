package com.hdu.mapper;

import com.github.pagehelper.Page;
import com.hdu.DTO.ForeignAppAccessLogDTO;
import com.hdu.entity.ForeignAppAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForeignAppAccessLogMapper {
    @Select("SELECT COALESCE(MAX(uid), 0) FROM foreign_app_access_log")
    int getUidFromForeignAppAccess();

    int insertForeignAppAccessBatch(List<ForeignAppAccessLog> logList);

    Page<ForeignAppAccessLog> listForeignAppAccessLog(ForeignAppAccessLogDTO foreignAppAccessLogDTO);
}
