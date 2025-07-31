package com.hdu.contorller;

import com.hdu.dto.TunnelAccessLogDTO;
import com.hdu.result.PageResult;
import com.hdu.result.Result;
import com.hdu.service.TunnelAccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/TunnelAccessLog")
@RequiredArgsConstructor
@Tag(name = "突网日志处理相关接口")
public class TunnelAccessLogController {
    private final TunnelAccessLogService tunnelAccessLogService;
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
        int uid = tunnelAccessLogService.parseTunnelAccessLog(file);
        return Result.success(uid);
    }

    /**
     * 分页查询TunnelAccess日志
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询TunnelAccess日志")
    public Result<PageResult> listWebAccessLog(@RequestBody TunnelAccessLogDTO tunnelAccessLogDTO) {
        log.info("分页查询TunnelAccess日志:{}", tunnelAccessLogDTO);
        PageResult pageResult = tunnelAccessLogService.listTunnelAccessLog(tunnelAccessLogDTO);
        return Result.success(pageResult);
    }

    /**
     * 导出TunnelAccess日志
     * @param response
     * @return
     */
    @PostMapping("/export")
    @Operation(summary = "导出TunnelAccess日志")
    public void exportWebAccessLog(HttpServletResponse response, @RequestBody TunnelAccessLogDTO tunnelAccessLogDTO) {
        log.info("导出TunnelAccess日志:{}", tunnelAccessLogDTO);
        tunnelAccessLogService.exportTunnelAccessLog(response, tunnelAccessLogDTO);
    }
}
