package com.hdu.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class PanabitSyncScheduler {

    private static final Path BASE_SCRIPT_PATH = Paths.get("nta-service-api", "scripts", "Panabit-API-Scripts", "sync_tools");

    private static final String DOMAIN_SYNC_SCRIPT_PATH =
            BASE_SCRIPT_PATH.resolve("Panabit_domain_sync.py").toAbsolutePath().toString();

    private static final String IP_SYNC_SCRIPT_PATH =
            BASE_SCRIPT_PATH.resolve("Panabit_ip_sync.py").toAbsolutePath().toString();

//    private static final String TEST_SCRIPT_PATH =
//            BASE_SCRIPT_PATH.resolve("test.py").toAbsolutePath().toString();


    private static final String PYTHON_EXEC = "python";

    @Scheduled(cron = "0 0 8 * * ?") // 每天早上 8 点
//    @Scheduled(cron = "*/10 * * * * ?") // 测试 每10秒一次
    public void runPanabitSyncScript() {
        log.info("⏰ 开始执行 Panabit 黑名单同步脚本...");

        runScript(DOMAIN_SYNC_SCRIPT_PATH);
        runScript(IP_SYNC_SCRIPT_PATH);
//        runScript(TEST_SCRIPT_PATH);

        log.info("✅ 所有 Panabit 同步脚本执行完成");
    }

    private void runScript(String scriptPath) {
        log.info("▶ 开始执行脚本：{}", scriptPath);

        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[PanabitSync] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✅ 脚本执行成功：{}", scriptPath);
            } else {
                log.warn("⚠️ 脚本异常退出，路径：{}，退出码：{}", scriptPath, exitCode);
            }
        } catch (Exception e) {
            log.error("❌ 执行脚本失败，路径：" + scriptPath, e);
        }
    }
}
