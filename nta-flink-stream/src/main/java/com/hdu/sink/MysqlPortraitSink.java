package com.hdu.sink;

import com.hdu.config.MysqlConfig;
import com.hdu.utils.ConfigUtils;
import com.hdu.entity.FlowImage;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MysqlPortraitSink extends RichSinkFunction<FlowImage> {
    private static final MysqlConfig mysqlConfig = ConfigUtils.getMysqlConfig();

    private static final String JDBC_URL = mysqlConfig.getUrl();
    private static final String USERNAME = mysqlConfig.getUsername();
    private static final String PASSWORD = mysqlConfig.getPassword();

    //  使用 ON DUPLICATE KEY UPDATE 以 flow_id 做幂等更新
    private static final String UPSERT_SQL =
            "INSERT INTO flow_image (" +
                    "flow_id, adsl_account, start_time, end_time, " +
                    "total_up_bytes, total_down_bytes, total_bytes, " +
                    "site_names, tools, tunnel_type, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "adsl_account=COALESCE(VALUES(adsl_account), adsl_account), " +
                    "start_time=COALESCE(VALUES(start_time), start_time), " +
                    "end_time=COALESCE(VALUES(end_time), end_time), " +
                    "total_up_bytes=COALESCE(VALUES(total_up_bytes), total_up_bytes), " +
                    "total_down_bytes=COALESCE(VALUES(total_down_bytes), total_down_bytes), " +
                    "total_bytes=COALESCE(VALUES(total_bytes), total_bytes), " +
                    "site_names=COALESCE(VALUES(site_names), site_names), " +
                    "tools=COALESCE(VALUES(tools), tools), " +
                    "tunnel_type=COALESCE(VALUES(tunnel_type), tunnel_type)";


    private static final int BATCH_SIZE = 100;
    private static final long FLUSH_INTERVAL_MS = 2000;

    private Connection conn;
    private PreparedStatement ps;
    private List<FlowImage> buffer;

    private transient ScheduledExecutorService scheduler;
    private transient ScheduledFuture<?> scheduledFlush;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        conn.setAutoCommit(false);
        ps = conn.prepareStatement(UPSERT_SQL);
        buffer = new ArrayList<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledFlush = scheduler.scheduleAtFixedRate(this::flush, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invoke(FlowImage flowImage, Context context) throws Exception {
        synchronized (this) {
            ps.setString(1, flowImage.getFlowId());
            ps.setString(2, flowImage.getAdslAccount());
            ps.setTimestamp(3, toTimestamp(flowImage.getStartTime()));
            ps.setTimestamp(4, toTimestamp(flowImage.getEndTime()));
            ps.setLong(5, flowImage.getTotalUpBytes());
            ps.setLong(6, flowImage.getTotalDownBytes());
            ps.setLong(7, flowImage.getTotalBytes());
            ps.setString(8, String.join(",", flowImage.getSiteNames()));
            ps.setString(9, String.join(",", flowImage.getTools()));
            ps.setString(10, flowImage.getTunnelType());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.addBatch();

            buffer.add(flowImage);

            if (buffer.size() >= BATCH_SIZE) {
                flush();
            }
        }
    }

    private synchronized void flush() {
        try {
            if (buffer.isEmpty()) return;

            ps.executeBatch();
            conn.commit();
            buffer.clear();
        } catch (Exception e) {
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

    private Timestamp toTimestamp(LocalDateTime time) {
        return time != null ? Timestamp.valueOf(time) : null;
    }
}
