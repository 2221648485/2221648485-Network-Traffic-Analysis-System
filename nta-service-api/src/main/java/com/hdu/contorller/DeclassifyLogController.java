package com.hdu.contorller;

import com.hdu.dto.DeclassifyLogDTO;
import com.hdu.result.PageResult;
import com.hdu.result.Result;
import com.hdu.service.DeclassifyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/DeclassifyLog")
@RequiredArgsConstructor
@Tag(name = "解密数据日志相关接口")
public class DeclassifyLogController {
    private final DeclassifyLogService declassifyLogService;

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
        int uid = declassifyLogService.parseDeclassifyLog(file);
        return Result.success(uid);
    }

    /**
     * 分页查询DeclassifyLog日志
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询DeclassifyLog日志")
    public Result<PageResult> listWebAccessLog(@RequestBody DeclassifyLogDTO declassifyLogDTO) {
        log.info("分页查询DeclassifyLog日志:{}", declassifyLogDTO);
        PageResult pageResult = declassifyLogService.listDeclassifyLog(declassifyLogDTO);
        return Result.success(pageResult);
    }

    /**
     * 导出DeclassifyLog日志
     * @param response
     * @return
     */
    @PostMapping("/export")
    @Operation(summary = "导出DeclassifyLog日志")
    public void exportWebAccessLog(HttpServletResponse response, @RequestBody DeclassifyLogDTO declassifyLogDTO) {
        log.info("导出DeclassifyLog日志:{}", declassifyLogDTO);
        declassifyLogService.exportDeclassifyLog(response, declassifyLogDTO);
    }
}
