package com.hdu.DTO;

import com.hdu.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedUserLogDTO {
    private String phoneNumber;

    // WebAccessLog
    private List<WebAccessLog> webLogs;

    // TunnelAccessLog
    private List<TunnelAccessLog> tunnelLogs;

    // TunnelOfflineLog
    private List<TunnelOfflineLog> offlineLogs;

    // ForeignAppAccessLog
    private List<ForeignAppAccessLog> foreignAppLogs;

    // DeclassifyLog
    private List<DeclassifyLog> declassifyLogs;

}
