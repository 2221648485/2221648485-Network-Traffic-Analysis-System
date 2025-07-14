package com.hdu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hdu.DTO.TunnelAccessLogDTO;
import com.hdu.entity.TunnelAccessLog;
import com.hdu.entity.WebAccessLog;
import com.hdu.mapper.TunnelAccessLogMapper;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TunnelAccessLogService {
    private final TunnelAccessLogMapper tunnelAccessLogMapper;
    public int parseTunnelAccessLog(MultipartFile file) {
        List<TunnelAccessLog> logList = new ArrayList<>();
        // 获取当前解析的UID
        int uid = tunnelAccessLogMapper.getUidFromTunnelAccess();
        uid = uid + 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // 去除前后空格
                // TunnelAccessLog 有一个静态方法 fromString 用于从字符串解析日志
                TunnelAccessLog log = TunnelAccessLog.fromString(line);
                if (log != null) {
                    log.setUid(uid);
                    logList.add(log);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入数据库中
        int flag = tunnelAccessLogMapper.insertTunnelAccessLogBatch(logList);
        System.out.println("flag: " + flag);
        return flag == 1 ? uid : uid - 1;
    }

    public PageResult listTunnelAccessLog(TunnelAccessLogDTO tunnelAccessLogDTO) {
        int uid = tunnelAccessLogMapper.getUidFromTunnelAccess();
        if (tunnelAccessLogDTO.getUid() == null) {
            tunnelAccessLogDTO.setUid(uid);
        }
        PageHelper.startPage(tunnelAccessLogDTO.getPage(), tunnelAccessLogDTO.getPageSize());
        Page<TunnelAccessLog> page = tunnelAccessLogMapper.listTunnelAccessLog(tunnelAccessLogDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void exportTunnelAccessLog(HttpServletResponse response, TunnelAccessLogDTO tunnelAccessLogDTO) {
        List<TunnelAccessLog> logs = tunnelAccessLogMapper.listTunnelAccessLog(tunnelAccessLogDTO);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/TunnelAccessLog模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            for (int i = 0; i < logs.size(); i++) {
                TunnelAccessLog log = logs.get(i);
                XSSFRow row = sheet.createRow(i + 3); // 从第5行开始

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                row.createCell(0).setCellValue(log.getTime() != null ? log.getTime().format(formatter) : "");
                row.createCell(1).setCellValue(log.getFlowId());
                row.createCell(2).setCellValue(log.getPhoneNumber());
                row.createCell(3).setCellValue(log.getImsi());
                row.createCell(4).setCellValue(log.getImei());
                row.createCell(5).setCellValue(log.getAdslAccount());
                row.createCell(6).setCellValue(log.getClientIp());
                row.createCell(7).setCellValue(log.getServerIp());
                row.createCell(8).setCellValue(log.getClientRegion());
                row.createCell(9).setCellValue(log.getServerRegion());
                row.createCell(10).setCellValue(log.getTunnelType());
                row.createCell(11).setCellValue(log.getOperator());
                row.createCell(12).setCellValue(log.getTool());
                row.createCell(13).setCellValue(log.getClientPort() != null ? log.getClientPort() : 0);
                row.createCell(14).setCellValue(log.getServerPort() != null ? log.getServerPort() : 0);
                row.createCell(15).setCellValue(log.getUpBytes() != null ? log.getUpBytes() : 0);
                row.createCell(16).setCellValue(log.getDownBytes() != null ? log.getDownBytes() : 0);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("突网行为日志.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 写出文件
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException("导出突网行为日志失败", e);
        }
    }
}
