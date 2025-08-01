package com.hdu.sink;

import com.hdu.config.MysqlConfig;
import com.hdu.result.RiskResult;
import com.hdu.utils.ConfigUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MysqlRiskSink extends RichSinkFunction<RiskResult> {
    private static final MysqlConfig mysqlConfig = ConfigUtils.getMysqlConfig();

    // 数据库连接配置（抽取为常量）
    private static final String JDBC_URL = mysqlConfig.getUrl();
    private static final String USERNAME = mysqlConfig.getUsername();
    private static final String PASSWORD = mysqlConfig.getPassword();
    private static final String INSERT_SQL = "INSERT INTO risk_result (phone_number, risk_level, window_start, window_end, msg, create_time, status, adsl_account) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final int BATCH_SIZE = 100; // 批量插入
    private static final long FLUSH_INTERVAL_MS = 2000; // 2秒定时刷新批量数据
    private Connection conn;
    private PreparedStatement ps;
    private List<RiskResult> buffer;
    private transient ScheduledExecutorService scheduler;
    private transient ScheduledFuture<?> scheduledFlush;

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver"); // 显式加载驱动
        super.open(parameters);
        conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        conn.setAutoCommit(false);  // 关闭自动提交，提升性能
        ps = conn.prepareStatement(INSERT_SQL);
        buffer = new ArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledFlush = scheduler.scheduleAtFixedRate(this::flush, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invoke(RiskResult risk, Context context) throws Exception {
        synchronized (this) {
            ps.setString(1, risk.getPhoneNumber());
            ps.setString(2, risk.getRiskLevel());
            ps.setTimestamp(3, toTimestamp(risk.getWindowStart()));
            ps.setTimestamp(4, toTimestamp(risk.getWindowEnd()));
            ps.setString(5, risk.getMsg());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, risk.getStatus());
            ps.setString(8, risk.getAdslAccount());
            ps.addBatch();

            buffer.add(risk);

            if (buffer.size() >= BATCH_SIZE) {
                flush();
            }
        }
    }

    private synchronized void flush() {
        try {
            if (buffer.isEmpty()) {
                return;
            }
            ps.executeBatch();
            conn.commit();
            buffer.clear();
        } catch (Exception e) {
            // 日志记录，异常处理
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
        synchronized (this) {
            flush();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        super.close();
    }

    // 辅助方法：安全转换 LocalDateTime 为 Timestamp
    private Timestamp toTimestamp(LocalDateTime time) {
        return time != null ? Timestamp.valueOf(time) : null;
    }
}