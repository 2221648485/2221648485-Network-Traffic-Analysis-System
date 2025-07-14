package com.hdu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.hdu.entity.*;
@Mapper
public interface ABMapper {
    ABMapper INSTANCE = Mappers.getMapper(ABMapper.class);

    UnifiedLog aToB(WebAccessLog webAccessLog);
    UnifiedLog aToB(TunnelAccessLog tunnelAccessLog);
    UnifiedLog aToB(TunnelOfflineLog tunnelOfflineLog);
    UnifiedLog aToB(ForeignAppAccessLog foreignAppAccessLog);
    UnifiedLog aToB(DeclassifyLog declassifyLog);

}
