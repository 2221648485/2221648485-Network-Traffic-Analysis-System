package com.hdu.sink;

import com.hdu.config.MysqlConfig;
import com.hdu.entity.UnifiedLog;
import com.hdu.utils.ConfigUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MysqlTestSink extends RichSinkFunction<UnifiedLog> {
    private static final MysqlConfig mysqlConfig = ConfigUtils.getMysqlConfig();
    private static final String JDBC_URL = mysqlConfig.getUrl();
    private static final String USERNAME = mysqlConfig.getUsername();
    private static final String PASSWORD = mysqlConfig.getPassword();
    private static final String INSERT_SQL = "INSERT INTO unified_log (" +
            "time, type, phone_number, imsi, imei, adsl_account, client_ip, server_ip, " +
            "client_region, server_region, tunnel_type, operator, tool, site_name, site_url, site_type, " +
            "client_port, server_port, up_bytes, down_bytes, credibility, flow_id, packet_index, original_file_name, " +
            "offline_time, total_bytes, app_name, network_protocol, app_protocol, app_info, host_name, uid, raw_line, create_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final int BATCH_SIZE = 100;
    private static final long FLUSH_INTERVAL_MS = 2000;

    private transient Connection conn;
    private transient PreparedStatement ps;
    private transient List<UnifiedLog> buffer;
    private transient ScheduledExecutorService scheduler;
    private transient ScheduledFuture<?> scheduledFlush;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        conn.setAutoCommit(false);
        ps = conn.prepareStatement(INSERT_SQL);
        buffer = new ArrayList<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledFlush = scheduler.scheduleAtFixedRate(this::flush, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invoke(UnifiedLog log, Context context) throws Exception {
        synchronized (this) {
            int idx = 1;
            ps.setTimestamp(idx++, toTimestamp(log.getTime()));
            ps.setString(idx++, log.getType());
            ps.setString(idx++, log.getPhoneNumber());
            ps.setString(idx++, log.getImsi());
            ps.setString(idx++, log.getImei());
            ps.setString(idx++, log.getAdslAccount());
            ps.setString(idx++, log.getClientIp());
            ps.setString(idx++, log.getServerIp());
            ps.setString(idx++, log.getClientRegion());
            ps.setString(idx++, log.getServerRegion());
            ps.setString(idx++, log.getTunnelType());
            ps.setString(idx++, log.getOperator());
            ps.setString(idx++, log.getTool());
            ps.setString(idx++, log.getSiteName());
            ps.setString(idx++, log.getSiteUrl());
            ps.setString(idx++, log.getSiteType());
            ps.setObject(idx++, log.getClientPort(), Types.INTEGER);
            ps.setObject(idx++, log.getServerPort(), Types.INTEGER);
            ps.setObject(idx++, log.getUpBytes(), Types.BIGINT);
            ps.setObject(idx++, log.getDownBytes(), Types.BIGINT);
            ps.setString(idx++, log.getCredibility());
            ps.setString(idx++, log.getFlowId());
            ps.setObject(idx++, log.getPacketIndex(), Types.INTEGER);
            ps.setString(idx++, log.getOriginalFileName());
            ps.setTimestamp(idx++, toTimestamp(log.getOfflineTime()));
            ps.setObject(idx++, log.getTotalBytes(), Types.BIGINT);
            ps.setString(idx++, log.getAppName());
            ps.setString(idx++, log.getNetworkProtocol());
            ps.setString(idx++, log.getAppProtocol());
            ps.setString(idx++, log.getAppInfo());
            ps.setString(idx++, log.getHostName());
            ps.setObject(idx++, log.getUid(), Types.INTEGER);
            ps.setString(idx++, log.getRawLine());
            ps.setTimestamp(idx++, toTimestamp(LocalDateTime.now()));

            ps.addBatch();
            buffer.add(log);

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
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
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
        return time == null ? null : Timestamp.valueOf(time);
    }

}
