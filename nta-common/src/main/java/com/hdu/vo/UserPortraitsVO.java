package com.hdu.vo;

import com.hdu.entity.FlowImage;
import com.hdu.entity.ForeignAppAccessLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPortraitsVO {

    private String name;                    // 用户姓名
    private String adslAccount;             // ADSL账号
    private String mobile;                  // 手机号码
    private String gender;                  // 性别
    private String RYLB;                    // 人员类别
    private String ZYMC;                    // 专业名称
    private String BJMC;                    // 班级名称
    private String YJSLB;                   // 研究生类别

    private List<FlowImage> flowImages;                    // 流量相关行为数据列表
    private List<ForeignAppAccessLog> foreignAppAccessLogs;// 境外App访问日志列表

}

