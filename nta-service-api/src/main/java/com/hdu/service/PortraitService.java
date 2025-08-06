package com.hdu.service;

import com.hdu.entity.FlowImage;
import com.hdu.mapper.mysql.PortraitMysqlMapper;
import com.hdu.mapper.sqlserver.PortraitSqlServerMapper;
import com.hdu.vo.UserPortraitsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortraitService {
    private final PortraitSqlServerMapper portraitSqlServerMapper;
    private final PortraitMysqlMapper portraitMysqlMapper;

    public UserPortraitsVO getPortrait(String adslAccount) {
        UserPortraitsVO userPortraitsVO = portraitSqlServerMapper.getInfoById(adslAccount);
        List<FlowImage> flowImagesByAdslAccount = portraitMysqlMapper.getFlowImagesByAdslAccount(adslAccount);
        userPortraitsVO.setFlowImages(flowImagesByAdslAccount);
        userPortraitsVO.setForeignAppAccessLogs(portraitMysqlMapper.getForeignAppAccessLogsByAdslAccount(adslAccount));
        return userPortraitsVO;
    }
}
