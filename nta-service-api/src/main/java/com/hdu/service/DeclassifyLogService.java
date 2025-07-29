package com.hdu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hdu.DTO.DeclassifyLogDTO;
import com.hdu.entity.DeclassifyLog;
import com.hdu.mapper.mysql.DeclassifyLogMapper;
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
public class DeclassifyLogService {
    private final DeclassifyLogMapper declassifyLogMapper;

    public int parseDeclassifyLog(MultipartFile file) {
        List<DeclassifyLog> logList = new ArrayList<>();
        // 获取当前解析的UID
        int uid = declassifyLogMapper.getUidFromDeclassify();
        uid = uid + 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // 去除前后空格
                // ForeignAppAccessLog 有一个静态方法 fromString 用于从字符串解析日志
                DeclassifyLog log = DeclassifyLog.fromString(line);
                if (log != null) {
                    log.setUid(uid);
                    logList.add(log);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入数据库中
        int flag = declassifyLogMapper.insertDeclassifyLogBatch(logList);
        System.out.println("flag: " + flag);
        return flag == 1 ? uid : uid - 1;
    }

    public PageResult listDeclassifyLog(DeclassifyLogDTO declassifyLogDTO) {
        int uid = declassifyLogMapper.getUidFromDeclassify();
        if (declassifyLogDTO.getUid() == null) {
            declassifyLogDTO.setUid(uid);
        }
        PageHelper.startPage(declassifyLogDTO.getPage(), declassifyLogDTO.getPageSize());
        Page<DeclassifyLog> page = declassifyLogMapper.listDeclassifyLog(declassifyLogDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void exportDeclassifyLog(HttpServletResponse response, DeclassifyLogDTO declassifyLogDTO) {
        List<DeclassifyLog> logs = declassifyLogMapper.listDeclassifyLog(declassifyLogDTO);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/DeclassifyLog模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            for (int i = 0; i < logs.size(); i++) {
                DeclassifyLog log = logs.get(i);
                XSSFRow row = sheet.createRow(i + 3); // 从第5行写入

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                row.createCell(0).setCellValue(log.getTime() != null ? log.getTime().format(formatter) : "");
                row.createCell(1).setCellValue(log.getFlowId());
                row.createCell(2).setCellValue(log.getPhoneNumber());
                row.createCell(3).setCellValue(log.getImsi());
                row.createCell(4).setCellValue(log.getImei());
                row.createCell(5).setCellValue(log.getAdslAccount());
                row.createCell(6).setCellValue(log.getClientIp());
                row.createCell(7).setCellValue(log.getClientPort() != null ? log.getClientPort() : 0);
                row.createCell(8).setCellValue(log.getServerIp());
                row.createCell(9).setCellValue(log.getServerPort() != null ? log.getServerPort() : 0);
                row.createCell(10).setCellValue(log.getClientRegion());
                row.createCell(11).setCellValue(log.getServerRegion());
                row.createCell(12).setCellValue(log.getOriginalFileName());
                row.createCell(13).setCellValue(log.getPacketIndex() != null ? log.getPacketIndex() : 0);
                row.createCell(14).setCellValue(log.getNetworkProtocol());
                row.createCell(15).setCellValue(log.getAppProtocol());
                row.createCell(16).setCellValue(log.getAppInfo());
                row.createCell(17).setCellValue(log.getHostName());
            }

            // 设置响应头
            String fileName = URLEncoder.encode("解密数据日志.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 写入并关闭
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException("导出解密数据日志失败", e);
        }
    }
}
