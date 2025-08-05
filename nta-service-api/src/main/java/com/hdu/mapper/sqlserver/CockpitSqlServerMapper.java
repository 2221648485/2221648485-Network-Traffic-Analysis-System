package com.hdu.mapper.sqlserver;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface CockpitSqlServerMapper {
    int getRealtimeSessions(LocalDateTime with, LocalDateTime now);
}
