package org.example.jubensha.common;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();

            // ─── PRAGMA for SQLite (harmless no-ops on MySQL) ───
            try { stmt.execute("PRAGMA journal_mode=WAL"); } catch (Exception ignored) {}
            try { stmt.execute("PRAGMA synchronous=NORMAL"); } catch (Exception ignored) {}
            try { stmt.execute("PRAGMA foreign_keys=ON"); } catch (Exception ignored) {}
            try { stmt.execute("PRAGMA busy_timeout=5000"); } catch (Exception ignored) {}

            // ==================== 核心用户表 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_user (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "phone VARCHAR(20) UNIQUE, " +
                "gender INTEGER DEFAULT 0, " +
                "hobby_type VARCHAR(100), " +
                "user_level INTEGER DEFAULT 1, " +
                "uid VARCHAR(50) NOT NULL, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "profile TEXT, " +
                "nickname VARCHAR(50), " +
                "real_name VARCHAR(50), " +
                "birthday DATE, " +
                "city VARCHAR(100), " +
                "avatar_url VARCHAR(255)" +
            ")");

            // ==================== 用户拓展记录表 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS user_registration_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "username VARCHAR(50) NOT NULL, " +
                "phone VARCHAR(20), " +
                "nickname VARCHAR(50), " +
                "registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "ip_address VARCHAR(50), " +
                "device_info VARCHAR(255), " +
                "status VARCHAR(20) DEFAULT 'active', " +
                "UNIQUE(user_id), " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            // ==================== 剧本核心：永久保留，绝不清除 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS script_series (" +
                "series_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "series_name VARCHAR(100) NOT NULL UNIQUE, " +
                "series_desc TEXT, " +
                "cover_url VARCHAR(255), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "background_url VARCHAR(255)" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS script (" +
                "script_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title VARCHAR(100) NOT NULL, " +
                "intro TEXT, " +
                "player_count INTEGER, " +
                "difficulty VARCHAR(50), " +
                "cover_url TEXT, " +
                "tags VARCHAR(255), " +
                "truth_content TEXT, " +
                "user_id INTEGER, " +
                "series_id INTEGER, " +
                "series_order INTEGER DEFAULT 0, " +
                "bgm_url VARCHAR(255), " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (series_id) REFERENCES script_series(series_id) ON DELETE SET NULL" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS script_publish_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "publish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'published', " +
                "view_count INTEGER DEFAULT 0, " +
                "play_count INTEGER DEFAULT 0, " +
                "favorite_count INTEGER DEFAULT 0, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS role (" +
                "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "name VARCHAR(50) NOT NULL, " +
                "is_ai INTEGER DEFAULT 0, " +
                "avatar VARCHAR(255), " +
                "background TEXT, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS act (" +
                "act_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "act_name VARCHAR(50), " +
                "sort INTEGER, " +
                "public_content TEXT, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS role_act_content (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "act_id INTEGER NOT NULL, " +
                "role_id INTEGER NOT NULL, " +
                "content TEXT, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (act_id) REFERENCES act(act_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS script_clue (" +
                "clue_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "location_name VARCHAR(100), " +
                "clue_name VARCHAR(100) NOT NULL, " +
                "clue_desc TEXT, " +
                "clue_image_url VARCHAR(255), " +
                "is_public INTEGER DEFAULT 1, " +
                "role_id INTEGER, " +
                "unlock_chapter_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "is_hidden INTEGER DEFAULT 0, " +
                "unlock_condition VARCHAR(255), " +
                "scene_image_url VARCHAR(255), " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS script_ending (" +
                "ending_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "voted_role_id INTEGER, " +
                "ending_title VARCHAR(100) NOT NULL, " +
                "ending_content TEXT, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS script_architecture (" +
                "arch_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "arch_name VARCHAR(100) NOT NULL, " +
                "image_url VARCHAR(255), " +
                "model_url VARCHAR(255), " +
                "arch_desc TEXT, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            // 迁移：旧库的 script_architecture 表补加 model_url 列（新库已含，重复加列会失败并忽略）
            try {
                stmt.execute("ALTER TABLE script_architecture ADD COLUMN model_url VARCHAR(255)");
            } catch (Exception ignored) {
                // 列已存在则忽略
            }

            // ==================== 游戏运行表 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS game_progress (" +
                "game_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "user_role_id INTEGER NOT NULL, " +
                "current_act_id INTEGER NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'playing', " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "phase VARCHAR(50) DEFAULT 'ROLE_SELECT', " +
                "voted_role_id INTEGER, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS game_unlocked_clue (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, " +
                "clue_id INTEGER NOT NULL, " +
                "unlock_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (game_id) REFERENCES game_progress(game_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (clue_id) REFERENCES script_clue(clue_id) ON DELETE CASCADE, " +
                "UNIQUE(game_id, clue_id)" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS game_chat_record (" +
                "chat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, " +
                "act_id INTEGER NOT NULL, " +
                "sender_role_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (game_id) REFERENCES game_progress(game_id) ON DELETE CASCADE" +
            ")");

            // ==================== 用户行为表 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS user_play_record (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "play_time TIMESTAMP, " +
                "play_duration INTEGER, " +
                "remark VARCHAR(500) DEFAULT '', " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_browse_history (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "target_id INTEGER NOT NULL, " +
                "target_type INTEGER NOT NULL, " +
                "browse_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_favorite (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "script_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(user_id, script_id), " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_follow (" +
                "follow_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "followed_user_id INTEGER NOT NULL, " +
                "follow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "UNIQUE(user_id, followed_user_id), " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (followed_user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_evaluate_record (" +
                "evaluate_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "score INTEGER NOT NULL, " +
                "content TEXT, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE" +
            ")");

            // ==================== 系统剧本 / 创作辅助表 ====================

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script (" +
                "script_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "cover_url VARCHAR(255), " +
                "description TEXT, " +
                "total_players INTEGER NOT NULL, " +
                "male_players INTEGER DEFAULT 0, " +
                "female_players INTEGER DEFAULT 0, " +
                "difficulty INTEGER DEFAULT 3, " +
                "tags VARCHAR(255), " +
                "author_id INTEGER, " +
                "status INTEGER DEFAULT 1, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (author_id) REFERENCES sys_user(user_id) ON DELETE SET NULL" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_chapter (" +
                "chapter_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "chapter_round INTEGER NOT NULL, " +
                "chapter_name VARCHAR(100) NOT NULL, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (script_id) REFERENCES sys_script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_character (" +
                "character_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "character_name VARCHAR(50) NOT NULL, " +
                "avatar_url VARCHAR(255), " +
                "gender INTEGER DEFAULT 2, " +
                "intro VARCHAR(500), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (script_id) REFERENCES sys_script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_clue (" +
                "clue_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "location_name VARCHAR(50) NOT NULL, " +
                "clue_name VARCHAR(100) NOT NULL, " +
                "clue_desc TEXT NOT NULL, " +
                "clue_image_url VARCHAR(255), " +
                "is_public INTEGER DEFAULT 0, " +
                "unlock_chapter_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (script_id) REFERENCES sys_script(script_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_content (" +
                "content_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chapter_id INTEGER NOT NULL, " +
                "character_id INTEGER NOT NULL, " +
                "content_text TEXT NOT NULL, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (chapter_id) REFERENCES sys_script_chapter(chapter_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (character_id) REFERENCES sys_script_character(character_id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_creation (" +
                "creation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "script_desc VARCHAR(1000) DEFAULT '', " +
                "script_type VARCHAR(50) DEFAULT '', " +
                "status INTEGER DEFAULT 0, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
            ")");

            // ==================== 性能索引 ====================

            createIndexIfNotExists(stmt, "idx_script_user_id", "script", "user_id");
            createIndexIfNotExists(stmt, "idx_script_series", "script", "series_id, series_order");
            createIndexIfNotExists(stmt, "idx_role_script", "role", "script_id");
            createIndexIfNotExists(stmt, "idx_act_script", "act", "script_id");
            createIndexIfNotExists(stmt, "idx_role_act_script", "role_act_content", "script_id, act_id");
            createIndexIfNotExists(stmt, "idx_role_act_act_role", "role_act_content", "act_id, role_id");
            createIndexIfNotExists(stmt, "idx_clue_script", "script_clue", "script_id");
            createIndexIfNotExists(stmt, "idx_clue_script_filter", "script_clue", "script_id, unlock_chapter_id, is_hidden");
            createIndexIfNotExists(stmt, "idx_ending_script", "script_ending", "script_id");
            createIndexIfNotExists(stmt, "idx_arch_script", "script_architecture", "script_id");
            createIndexIfNotExists(stmt, "idx_game_progress_user", "game_progress", "user_id");
            createIndexIfNotExists(stmt, "idx_game_progress_script_status", "game_progress", "script_id, status");
            createIndexIfNotExists(stmt, "idx_chat_game", "game_chat_record", "game_id");
            createIndexIfNotExists(stmt, "idx_chat_game_act", "game_chat_record", "game_id, act_id");
            createIndexIfNotExists(stmt, "idx_play_user", "user_play_record", "user_id");
            createIndexIfNotExists(stmt, "idx_eval_user", "user_evaluate_record", "user_id");
            createIndexIfNotExists(stmt, "idx_eval_script", "user_evaluate_record", "script_id");
            createIndexIfNotExists(stmt, "idx_browse_user", "user_browse_history", "user_id, browse_time");

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createIndexIfNotExists(Statement stmt, String indexName, String table, String columns) {
        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + " (" + columns + ")");
        } catch (Exception e) {
            // MySQL 5.7 不支持 IF NOT EXISTS，降级处理
            try {
                stmt.execute("CREATE INDEX " + indexName + " ON " + table + " (" + columns + ")");
            } catch (Exception ignored) {
                // 索引已存在则忽略
            }
        }
    }
}
