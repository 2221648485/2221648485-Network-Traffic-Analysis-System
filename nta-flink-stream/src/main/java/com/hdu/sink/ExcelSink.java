package com.hdu.sink;

import com.hdu.entity.UnifiedLog;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelSink extends RichSinkFunction<UnifiedLog> {

    private final String filePath;
    private transient List<UnifiedLog> buffer;
    private final int flushThreshold = 500; // 超过该阈值就写入

    public ExcelSink(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void open(Configuration parameters) {
        buffer = new ArrayList<>();
    }

    @Override
    public void invoke(UnifiedLog value, Context context) {
        buffer.add(value);
        if (buffer.size() >= flushThreshold) {
            flushToExcel();
            buffer.clear();
        }
    }

    @Override
    public void close() {
        if (!buffer.isEmpty()) {
            flushToExcel();
            buffer.clear();
        }
    }

    private void flushToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("UnifiedLogs");
            int rowNum = 0;

            // 获取所有字段
            Field[] fields = UnifiedLog.class.getDeclaredFields();
            List<Field> fieldList = new ArrayList<>();
            for (Field field : fields) {
                if (field.getName().equals("rawLine")) continue; // 可选：不写入原始行
                field.setAccessible(true);
                fieldList.add(field);
            }

            // 写入表头
            Row header = sheet.createRow(rowNum++);
            for (int i = 0; i < fieldList.size(); i++) {
                header.createCell(i).setCellValue(fieldList.get(i).getName());
            }

            // 写入数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (UnifiedLog log : buffer) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < fieldList.size(); i++) {
                    Field field = fieldList.get(i);
                    Object value = field.get(log);
                    Cell cell = row.createCell(i);

                    if (value instanceof java.time.LocalDateTime) {
                        cell.setCellValue(((java.time.LocalDateTime) value).format(formatter));
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            // 自动列宽
            for (int i = 0; i < fieldList.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }

        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
