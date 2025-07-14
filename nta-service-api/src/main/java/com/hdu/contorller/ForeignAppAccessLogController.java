package com.hdu.contorller;

import com.hdu.DTO.ForeignAppAccessLogDTO;
import com.hdu.result.PageResult;
import com.hdu.result.Result;
import com.hdu.service.ForeignAppAccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/ForeignAppAccessLog")
@RequiredArgsConstructor
@Tag(name = "境外app使用行为处理相关接口")
public class ForeignAppAccessLogController {
    private final ForeignAppAccessLogService foreignAppAccessLogService;
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
        int uid = foreignAppAccessLogService.parseForeignAppAccessLog(file);
        return Result.success(uid);
    }

    /**
     * 分页查询ForeignAppAccessLog日志
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询ForeignAppAccessLog日志")
    public Result<PageResult> listWebAccessLog(@RequestBody ForeignAppAccessLogDTO foreignAppAccessLogDTO) {
        log.info("分页查询ForeignAppAccessLog日志:{}", foreignAppAccessLogDTO);
        PageResult pageResult = foreignAppAccessLogService.listForeignAppAccessLog(foreignAppAccessLogDTO);
        return Result.success(pageResult);
    }

    /**
     * 导出ForeignAppAccessLog日志
     * @param response
     * @return
     */
    @PostMapping("/export")
    @Operation(summary = "导出ForeignAppAccessLog日志")
    public void exportWebAccessLog(HttpServletResponse response, @RequestBody ForeignAppAccessLogDTO foreignAppAccessLogDTO) {
        log.info("导出ForeignAppAccessLog日志:{}", foreignAppAccessLogDTO);
        foreignAppAccessLogService.exportForeignAppAccessLog(response, foreignAppAccessLogDTO);
    }
}
