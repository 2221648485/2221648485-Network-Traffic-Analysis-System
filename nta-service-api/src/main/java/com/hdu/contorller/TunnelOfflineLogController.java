package com.hdu.contorller;

import com.hdu.dto.TunnelOfflineLogDTO;
import com.hdu.result.PageResult;
import com.hdu.result.Result;
import com.hdu.service.TunnelOfflineLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/TunnelOfflineLog")
@RequiredArgsConstructor
@Tag(name = "突网行为下线日志处理相关接口")
public class TunnelOfflineLogController {
    private final TunnelOfflineLogService tunnelOfflineLogService;
    /**
     * 上传日志文件
     *
     * @return
     */
    @PostMapping("/parse")
    @Operation(summary = "上传日志文件")
    public Result<Integer> parseWebAccessLog(@RequestParam("file") MultipartFile file) {
        log.info("文件上传成功:{}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return Result.error("文件为空！");
        }

        if (!file.getOriginalFilename().endsWith(".txt")) {
            return Result.error("文件格式错误！只支持txt格式");
        }
        // 解析日志文件并加入数据库
        int uid = tunnelOfflineLogService.parseTunnelOfflineLog(file);
        return Result.success(uid);
    }

    /**
     * 分页查询TunnelOfflineLog日志
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询TunnelOfflineLog日志")
    public Result<PageResult> listWebAccessLog(@RequestBody TunnelOfflineLogDTO tunnelOfflineLogDTO) {
        log.info("分页查询TunnelOfflineLog日志:{}", tunnelOfflineLogDTO);
        PageResult pageResult = tunnelOfflineLogService.listTunnelOfflineLog(tunnelOfflineLogDTO);
        return Result.success(pageResult);
    }

    /**
     * 导出TunnelOfflineLog日志
     * @param response
     * @return
     */
    @PostMapping("/export")
    @Operation(summary = "导出TunnelOfflineLog日志")
    public void exportWebAccessLog(HttpServletResponse response, @RequestBody TunnelOfflineLogDTO tunnelOfflineLogDTO) {
        log.info("导出TunnelOfflineLog日志:{}", tunnelOfflineLogDTO);
        tunnelOfflineLogService.exportTunnelOfflineLog(response, tunnelOfflineLogDTO);
    }
}
