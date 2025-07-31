package com.hdu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hdu.dto.ForeignAppAccessLogDTO;
import com.hdu.entity.ForeignAppAccessLog;
import com.hdu.mapper.mysql.ForeignAppAccessLogMapper;
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
public class ForeignAppAccessLogService {
    private final ForeignAppAccessLogMapper foreignAppAccessLogMapper;
    public int parseForeignAppAccessLog(MultipartFile file) {
        List<ForeignAppAccessLog> logList = new ArrayList<>();
        // 获取当前解析的UID
        int uid = foreignAppAccessLogMapper.getUidFromForeignAppAccess();
        uid = uid + 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // 去除前后空格
                // ForeignAppAccessLog 有一个静态方法 fromString 用于从字符串解析日志
                ForeignAppAccessLog log = ForeignAppAccessLog.fromString(line);
                if (log != null) {
                    log.setUid(uid);
                    logList.add(log);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入数据库中
        int flag = foreignAppAccessLogMapper.insertForeignAppAccessBatch(logList);
        System.out.println("flag: " + flag);
        return flag == 1 ? uid : uid - 1;
    }

    public PageResult listForeignAppAccessLog(ForeignAppAccessLogDTO foreignAppAccessLogDTO) {
        int uid = foreignAppAccessLogMapper.getUidFromForeignAppAccess();
        if (foreignAppAccessLogDTO.getUid() == null) {
            foreignAppAccessLogDTO.setUid(uid);
        }
        PageHelper.startPage(foreignAppAccessLogDTO.getPage(), foreignAppAccessLogDTO.getPageSize());
        Page<ForeignAppAccessLog> page = foreignAppAccessLogMapper.listForeignAppAccessLog(foreignAppAccessLogDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void exportForeignAppAccessLog(HttpServletResponse response, ForeignAppAccessLogDTO foreignAppAccessLogDTO) {
        List<ForeignAppAccessLog> logs = foreignAppAccessLogMapper.listForeignAppAccessLog(foreignAppAccessLogDTO);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/ForeignAppAccess模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            for (int i = 0; i < logs.size(); i++) {
                ForeignAppAccessLog log = logs.get(i);
                XSSFRow row = sheet.createRow(i + 3); // 从第5行写入数据

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                row.createCell(0).setCellValue(log.getTime() != null ? log.getTime().format(formatter) : "");
                row.createCell(1).setCellValue(log.getPhoneNumber());
                row.createCell(2).setCellValue(log.getImsi());
                row.createCell(3).setCellValue(log.getImei());
                row.createCell(4).setCellValue(log.getAdslAccount());
                row.createCell(5).setCellValue(log.getClientIp());
                row.createCell(6).setCellValue(log.getServerIp());
                row.createCell(7).setCellValue(log.getAppName());
            }

            // 设置响应头
            String fileName = URLEncoder.encode("境外APP使用日志.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 写出文件
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException("导出境外APP访问日志失败", e);
        }
    }
}
