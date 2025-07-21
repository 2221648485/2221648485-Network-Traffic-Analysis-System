package com.hdu.sink;

import com.hdu.result.RiskResult;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class MysqlRiskSink extends RichSinkFunction<RiskResult> {
    // 数据库连接配置（抽取为常量）
//    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/Network-Traffic-Analysis-System";
    private static final String JDBC_URL = "jdbc:mysql://mysql:3306/Network_Traffic_Analysis_System?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String INSERT_SQL = "INSERT INTO risk_result (phone_number, risk_level, window_start, window_end, msg, create_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private Connection conn;
    private PreparedStatement ps;

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver"); // 显式加载驱动
        super.open(parameters);
        conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        ps = conn.prepareStatement(INSERT_SQL);
    }

    @Override
    public void invoke(RiskResult risk, Context context) throws Exception {
        ps.setString(1, risk.getPhoneNumber());
        ps.setString(2, risk.getRiskLevel());
        ps.setTimestamp(3, toTimestamp(risk.getWindowStart()));
        ps.setTimestamp(4, toTimestamp(risk.getWindowEnd()));
        System.out.println(risk.getMsg());
        ps.setString(5, risk.getMsg());
        ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(7, risk.getStatus());
        ps.executeUpdate();
    }

    @Override
    public void close() throws Exception {
        if (ps != null) ps.close();
        if (conn != null) conn.close();
    }

    // 辅助方法：安全转换 LocalDateTime 为 Timestamp
    private Timestamp toTimestamp(LocalDateTime time) {
        return time != null ? Timestamp.valueOf(time) : null;
    }
}