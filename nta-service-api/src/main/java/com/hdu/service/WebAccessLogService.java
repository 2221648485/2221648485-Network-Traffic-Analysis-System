package com.hdu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hdu.DTO.WebAccessLogDTO;
import com.hdu.entity.WebAccessLog;
import com.hdu.mapper.WebAccessLogMapper;
import com.hdu.result.PageResult;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebAccessLogService {

    private final WebAccessLogMapper webAccessLogMapper;

    /**
     * 解析 WebAccessLog 文件
     *
     * @param file 上传的文件
     * @return 解析后的 WebAccessLog 列表
     */
    public int parseWebAccessLog(MultipartFile file) {
        List<WebAccessLog> logList = new ArrayList<>();
        // 获取当前解析的UID
        int uid = webAccessLogMapper.getUidFromWebAccess();
        uid = uid + 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // 去除前后空格
                // WebAccessLog 有一个静态方法 fromString 用于从字符串解析日志
                WebAccessLog log = WebAccessLog.fromString(line);
                if (log != null) {
                    log.setUid(uid);
                    logList.add(log);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入数据库中
        int flag = webAccessLogMapper.insertWebAccessLogBatch(logList);
        System.out.println("flag: " + flag);
        return flag == 1 ? uid : uid - 1;
    }

    /**
     * 分页查询 WebAccessLog
     *
     * @param webAccessLogDTO 分页查询条件
     * @return 分页结果
     */
    public PageResult listWebAccessLog(WebAccessLogDTO webAccessLogDTO) {
        int uid = webAccessLogMapper.getUidFromWebAccess();
        if (webAccessLogDTO.getUid() == null) {
            webAccessLogDTO.setUid(uid);
        }
        PageHelper.startPage(webAccessLogDTO.getPage(), webAccessLogDTO.getPageSize());
        Page<WebAccessLog> page = webAccessLogMapper.listWebAccessLog(webAccessLogDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void exportWebAccessLog(HttpServletResponse response, WebAccessLogDTO webAccessLogDTO) {
        Page<WebAccessLog> webAccessLogs = webAccessLogMapper.listWebAccessLog(webAccessLogDTO);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/WebAccessLog模板.xlsx");
        try {
            // 基于模板创建文件
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            // 获取表格的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 填充数据
            for (int i = 0; i < webAccessLogs.size(); ++i) {
                WebAccessLog webAccessLog = webAccessLogs.get(i);
                XSSFRow row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(String.valueOf(webAccessLog.getTime()));
                row.createCell(1).setCellValue(webAccessLog.getPhoneNumber());
                row.createCell(2).setCellValue(webAccessLog.getImsi());
                row.createCell(3).setCellValue(webAccessLog.getImei());
                row.createCell(4).setCellValue(webAccessLog.getAdslAccount());
                row.createCell(5).setCellValue(webAccessLog.getSiteName());
                row.createCell(6).setCellValue(webAccessLog.getSiteUrl());
                row.createCell(7).setCellValue(webAccessLog.getSiteType());
                row.createCell(8).setCellValue(webAccessLog.getClientIp());
                row.createCell(9).setCellValue(webAccessLog.getServerIp());
                row.createCell(10).setCellValue(webAccessLog.getClientRegion());
                row.createCell(11).setCellValue(webAccessLog.getServerRegion());
                row.createCell(12).setCellValue(webAccessLog.getTunnelType());
                row.createCell(13).setCellValue(webAccessLog.getOperator());
                row.createCell(14).setCellValue(webAccessLog.getTool());
                row.createCell(15).setCellValue(webAccessLog.getClientPort() != null ? webAccessLog.getClientPort() : 0);
                row.createCell(16).setCellValue(webAccessLog.getServerPort() != null ? webAccessLog.getServerPort() : 0);
                row.createCell(17).setCellValue(webAccessLog.getUpBytes() != null ? webAccessLog.getUpBytes() : 0);
                row.createCell(18).setCellValue(webAccessLog.getDownBytes() != null ? webAccessLog.getDownBytes() : 0);
                row.createCell(19).setCellValue(webAccessLog.getCredibility());
            }
            // 设置响应头（文件名可自定义）
            String fileName = URLEncoder.encode("网络访问行为日志.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException("导出突网下线日志失败", e);
        }
    }
}
