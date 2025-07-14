package com.hdu.mapper;

import com.github.pagehelper.Page;
import com.hdu.DTO.DeclassifyLogDTO;
import com.hdu.entity.DeclassifyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeclassifyLogMapper {
    @Select("SELECT COALESCE(MAX(uid), 0) FROM declassify_log")
    int getUidFromDeclassify();

    int insertDeclassifyLogBatch(List<DeclassifyLog> logList);

    Page<DeclassifyLog> listDeclassifyLog(DeclassifyLogDTO declassifyLogDTO);
}
