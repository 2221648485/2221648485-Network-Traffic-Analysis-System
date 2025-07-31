package com.hdu.mapper.mysql;

import com.github.pagehelper.Page;
import com.hdu.dto.WebAccessLogDTO;
import com.hdu.entity.WebAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WebAccessLogMapper {

    @Select("SELECT COALESCE(MAX(uid), 0) FROM web_access_log")
    int getUidFromWebAccess();

    int insertWebAccessLogBatch(List<WebAccessLog> list);

    Page<WebAccessLog> listWebAccessLog(WebAccessLogDTO webAccessLogDTO);
}
