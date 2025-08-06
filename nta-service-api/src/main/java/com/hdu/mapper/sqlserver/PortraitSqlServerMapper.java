package com.hdu.mapper.sqlserver;

import com.hdu.vo.UserPortraitsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;

@Mapper
public interface PortraitSqlServerMapper {
    UserPortraitsVO getInfoById(@Param("adslAccount") String adslAccount);
}
