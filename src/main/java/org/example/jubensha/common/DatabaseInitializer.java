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
            
            // 创建用户表
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

            // 创建用户注册历史表（永久记录）
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
                "UNIQUE(user_id)" +
            ")");

            // 创建剧本发布记录表（永久记录）
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
                "favorite_count INTEGER DEFAULT 0" +
            ")");

            // 创建剧本表
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
                "bgm_url VARCHAR(255)" +
            ")");

            // 创建角色表
            stmt.execute("CREATE TABLE IF NOT EXISTS role (" +
                "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "name VARCHAR(50) NOT NULL, " +
                "is_ai INTEGER DEFAULT 0, " +
                "avatar VARCHAR(255), " +
                "background TEXT" +
            ")");

            // 创建幕次表
            stmt.execute("CREATE TABLE IF NOT EXISTS act (" +
                "act_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "act_name VARCHAR(50), " +
                "sort INTEGER, " +
                "public_content TEXT" +
            ")");

            // 创建角色剧本内容表
            stmt.execute("CREATE TABLE IF NOT EXISTS role_act_content (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "act_id INTEGER NOT NULL, " +
                "role_id INTEGER NOT NULL, " +
                "content TEXT" +
            ")");

            // 创建线索表
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
                "scene_image_url VARCHAR(255)" +
            ")");

            // 创建结局表
            stmt.execute("CREATE TABLE IF NOT EXISTS script_ending (" +
                "ending_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "voted_role_id INTEGER, " +
                "ending_title VARCHAR(100) NOT NULL, " +
                "ending_content TEXT" +
            ")");

            // 创建系列表
            stmt.execute("CREATE TABLE IF NOT EXISTS script_series (" +
                "series_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "series_name VARCHAR(100) NOT NULL UNIQUE, " +
                "series_desc TEXT, " +
                "cover_url VARCHAR(255), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "background_url VARCHAR(255)" +
            ")");

            // 创建古建表
            stmt.execute("CREATE TABLE IF NOT EXISTS script_architecture (" +
                "arch_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "arch_name VARCHAR(100) NOT NULL, " +
                "image_url VARCHAR(255), " +
                "arch_desc TEXT" +
            ")");

            // 创建游戏进度表
            stmt.execute("CREATE TABLE IF NOT EXISTS game_progress (" +
                "game_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "user_role_id INTEGER NOT NULL, " +
                "current_act_id INTEGER NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'playing', " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "phase VARCHAR(50) DEFAULT 'ROLE_SELECT', " +
                "voted_role_id INTEGER" +
            ")");

            // 创建线索解锁记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS game_unlocked_clue (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, " +
                "clue_id INTEGER NOT NULL, " +
                "unlock_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

            // 创建聊天记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS game_chat_record (" +
                "chat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, " +
                "act_id INTEGER NOT NULL, " +
                "sender_role_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

            // 创建用户玩本记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_play_record (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "play_time TIMESTAMP, " +
                "play_duration INTEGER, " +
                "remark VARCHAR(500) DEFAULT '', " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建浏览历史表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_browse_history (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "target_id INTEGER NOT NULL, " +
                "target_type INTEGER NOT NULL, " +
                "browse_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建收藏表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_favorite (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "script_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(user_id, script_id)" +
            ")");

            // 创建关注表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_follow (" +
                "follow_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "followed_user_id INTEGER NOT NULL, " +
                "follow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "UNIQUE(user_id, followed_user_id)" +
            ")");

            // 创建评价记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_evaluate_record (" +
                "evaluate_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "score INTEGER NOT NULL, " +
                "content TEXT, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建系统剧本表
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
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建剧本章节表
            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_chapter (" +
                "chapter_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "chapter_round INTEGER NOT NULL, " +
                "chapter_name VARCHAR(100) NOT NULL, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建剧本角色表
            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_character (" +
                "character_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "script_id INTEGER NOT NULL, " +
                "character_name VARCHAR(50) NOT NULL, " +
                "avatar_url VARCHAR(255), " +
                "gender INTEGER DEFAULT 2, " +
                "intro VARCHAR(500), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建剧本线索表
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
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建剧本正文表
            stmt.execute("CREATE TABLE IF NOT EXISTS sys_script_content (" +
                "content_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chapter_id INTEGER NOT NULL, " +
                "character_id INTEGER NOT NULL, " +
                "content_text TEXT NOT NULL, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            // 创建用户创作表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_creation (" +
                "creation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "script_name VARCHAR(100) NOT NULL, " +
                "script_desc VARCHAR(1000) DEFAULT '', " +
                "script_type VARCHAR(50) DEFAULT '', " +
                "status INTEGER DEFAULT 0, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted INTEGER DEFAULT 0" +
            ")");

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}