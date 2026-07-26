package org.example.jubensha.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;

/**
 * 定期清理非剧本类的增长数据。
 * 硬规则: 绝对不碰 script / role / act / script_clue / script_ending / script_architecture 表。
 * 在线房间聊天不受影响（完全在内存中）。
 */
@Component
public class DataCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataCleanupScheduler.class);

    @Autowired
    private DataSource dataSource;

    @Value("${cleanup.browse-retention-days:30}")
    private int browseRetentionDays;

    @Value("${cleanup.game-retention-days:30}")
    private int gameRetentionDays;

    @Value("${cleanup.voice-retention-days:7}")
    private int voiceRetentionDays;

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 每天凌晨 3:30 执行清理
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupAll() {
        log.info("=== 开始定时数据清理 ===");
        cleanupBrowseHistory();
        cleanupFinishedGameChat();
        cleanupFinishedGameProgress();
        cleanupOldPlayRecords();
        cleanupVoiceFiles();
        log.info("=== 定时数据清理完成 ===");
    }

    private void cleanupBrowseHistory() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                "DELETE FROM user_browse_history WHERE browse_time < datetime('now', '-%d days')",
                browseRetentionDays
            );
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                log.info("清理浏览历史: {} 条 (超过 {} 天)", deleted, browseRetentionDays);
            }
        } catch (Exception e) {
            log.warn("清理浏览历史失败: {}", e.getMessage());
        }
    }

    private void cleanupFinishedGameChat() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                "DELETE FROM game_chat_record WHERE game_id IN (" +
                "  SELECT g.game_id FROM game_progress g " +
                "  WHERE g.status IN ('end','FINISHED') " +
                "  AND g.create_time < datetime('now', '-%d days')" +
                ")", gameRetentionDays
            );
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                log.info("清理已完成游戏聊天记录: {} 条 (超过 {} 天)", deleted, gameRetentionDays);
            }
        } catch (Exception e) {
            log.warn("清理游戏聊天记录失败: {}", e.getMessage());
        }
    }

    private void cleanupFinishedGameProgress() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                "DELETE FROM game_progress WHERE status IN ('end','FINISHED') " +
                "AND create_time < datetime('now', '-%d days')",
                gameRetentionDays
            );
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                log.info("清理已完成游戏进度: {} 条 (超过 {} 天)", deleted, gameRetentionDays);
            }
        } catch (Exception e) {
            log.warn("清理游戏进度失败: {}", e.getMessage());
        }
    }

    private void cleanupOldPlayRecords() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format(
                "UPDATE user_play_record SET is_deleted = 1 " +
                "WHERE create_time < datetime('now', '-%d days') AND is_deleted = 0",
                90
            );
            int updated = stmt.executeUpdate(sql);
            if (updated > 0) {
                log.info("软删除旧游玩记录: {} 条 (超过 90 天)", updated);
            }
        } catch (Exception e) {
            log.warn("清理游玩记录失败: {}", e.getMessage());
        }
    }

    private void cleanupVoiceFiles() {
        try {
            Path voicesDir = Paths.get(uploadPath).resolve("voices").toAbsolutePath().normalize();
            if (!Files.isDirectory(voicesDir)) {
                return;
            }
            long cutoff = Instant.now().minusSeconds(voiceRetentionDays * 86400L).toEpochMilli();
            int deleted = 0;
            try (var stream = Files.list(voicesDir)) {
                for (Path file : stream.toList()) {
                    try {
                        if (Files.getLastModifiedTime(file).toMillis() < cutoff) {
                            Files.deleteIfExists(file);
                            deleted++;
                        }
                    } catch (IOException ignored) {
                        // 个别文件删除失败不影响整体
                    }
                }
            }
            if (deleted > 0) {
                log.info("清理旧语音文件: {} 个 (超过 {} 天)", deleted, voiceRetentionDays);
            }
        } catch (Exception e) {
            log.warn("清理语音文件失败: {}", e.getMessage());
        }
    }
}
