package com.hdu.contorller;

import com.hdu.dto.WebAccessLogDTO;
import com.hdu.result.PageResult;
import com.hdu.service.WebAccessLogService;
import com.hdu.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/WebAccessLog")
@RequiredArgsConstructor
@Tag(name = "网站访问日志处理相关接口")
public class WebAccessLogController {
    private final WebAccessLogService webAccessLogService;

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
        int uid = webAccessLogService.parseWebAccessLog(file);
        return Result.success(uid);
    }

    /**
     * 分页查询WebAccess日志
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询WebAccess日志")
    public Result<PageResult> listWebAccessLog(@RequestBody WebAccessLogDTO webAccessLogDTO) {
        log.info("分页查询WebAccess日志:{}", webAccessLogDTO);
        PageResult pageResult = webAccessLogService.listWebAccessLog(webAccessLogDTO);
        return Result.success(pageResult);
    }

    /**
     * 导出WebAccess日志
     * @param response
     * @return
     */
    @PostMapping("/export")
    @Operation(summary = "导出WebAccess日志")
    public void exportWebAccessLog(@RequestBody WebAccessLogDTO webAccessLogDTO, HttpServletResponse response) {
        log.info("导出WebAccess日志:{}", webAccessLogDTO);
        webAccessLogService.exportWebAccessLog(response, webAccessLogDTO);
    }
}
