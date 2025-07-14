package com.hdu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hdu.DTO.TunnelOfflineLogDTO;
import com.hdu.entity.TunnelOfflineLog;
import com.hdu.mapper.TunnelOfflineLogMapper;
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
public class TunnelOfflineLogService {
    private final TunnelOfflineLogMapper tunnelOfflineLogMapper;

    public int parseTunnelOfflineLog(MultipartFile file) {
        List<TunnelOfflineLog> logList = new ArrayList<>();
        // 获取当前解析的UID
        int uid = tunnelOfflineLogMapper.getUidFromTunnelOffLine();
        uid = uid + 1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // 去除前后空格
                // TunnelOfflineLog 有一个静态方法 fromString 用于从字符串解析日志
                TunnelOfflineLog log = TunnelOfflineLog.fromString(line);
                if (log != null) {
                    log.setUid(uid);
                    logList.add(log);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入数据库中
        int flag = tunnelOfflineLogMapper.insertTunnelOfflineLogBatch(logList);
        System.out.println("flag: " + flag);
        return flag == 1 ? uid : uid - 1;
    }

    public PageResult listTunnelOfflineLog(TunnelOfflineLogDTO tunnelOfflineLogDTO) {
        int uid = tunnelOfflineLogMapper.getUidFromTunnelOffLine();
        if (tunnelOfflineLogDTO.getUid() == null) {
            tunnelOfflineLogDTO.setUid(uid);
        }
        PageHelper.startPage(tunnelOfflineLogDTO.getPage(), tunnelOfflineLogDTO.getPageSize());
        Page<TunnelOfflineLog> page = tunnelOfflineLogMapper.listTunnelOfflineLog(tunnelOfflineLogDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    public void exportTunnelOfflineLog(HttpServletResponse response, TunnelOfflineLogDTO tunnelOfflineLogDTO) {
        // 查询数据
        List<TunnelOfflineLog> logs = tunnelOfflineLogMapper.listTunnelOfflineLog(tunnelOfflineLogDTO);
        // 加载 Excel 模板
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/TunnelOfflineLog模板.xlsx");
        try {
            // 创建 Excel 文件对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 写入数据，从第5行（索引=4）开始，前面保留表头
            for (int i = 0; i < logs.size(); ++i) {
                TunnelOfflineLog log = logs.get(i);
                XSSFRow row = sheet.createRow(i + 3); // 第4行之后开始写数据

                row.createCell(0).setCellValue(log.getFlowId());
                row.createCell(1).setCellValue(
                        log.getOfflineTime() != null ? log.getOfflineTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                row.createCell(2).setCellValue(log.getTotalBytes() != null ? log.getTotalBytes() : 0);
            }

            // 设置响应头（文件名可自定义）
            String fileName = URLEncoder.encode("突网行为下线日志.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 输出 Excel 文件
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException("导出突网下线日志失败", e);
        }
    }
}
