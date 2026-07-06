-- =====================================================
-- 剧本杀系统数据库表结构
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT,
    avatar TEXT,
    email TEXT,
    phone TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);

-- 剧本系列表（用于RPG闯关模式）
CREATE TABLE IF NOT EXISTS script_series (
    series_id INTEGER PRIMARY KEY AUTOINCREMENT,
    series_name TEXT NOT NULL,
    series_desc TEXT,
    cover_url TEXT,
    background_url TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);

-- 剧本表
CREATE TABLE IF NOT EXISTS script (
    script_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    intro TEXT,
    player_count INTEGER DEFAULT 4,
    difficulty TEXT DEFAULT '1',
    cover_url TEXT,
    tags TEXT,
    truth_content TEXT,
    user_id INTEGER,
    series_id INTEGER,
    series_order INTEGER,
    bgm_url TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    FOREIGN KEY (series_id) REFERENCES script_series(series_id)
);

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    role_id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    is_ai INTEGER DEFAULT 0,
    avatar TEXT,
    background TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE
);

-- 幕次表
CREATE TABLE IF NOT EXISTS act (
    act_id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    act_name TEXT NOT NULL,
    sort INTEGER DEFAULT 0,
    public_content TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE
);

-- 角色专属剧情内容表
CREATE TABLE IF NOT EXISTS role_act_content (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    act_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    content TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE,
    FOREIGN KEY (act_id) REFERENCES act(act_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
    UNIQUE(script_id, act_id, role_id)
);

-- 线索表
CREATE TABLE IF NOT EXISTS script_clue (
    clue_id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    clue_name TEXT NOT NULL,
    clue_desc TEXT,
    is_public INTEGER DEFAULT 1,
    role_id INTEGER,
    unlock_chapter_id INTEGER,
    is_hidden INTEGER DEFAULT 0,
    unlock_condition TEXT,
    scene_image_url TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE
);

-- 结局表
CREATE TABLE IF NOT EXISTS script_ending (
    ending_id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    voted_role_id INTEGER,
    ending_title TEXT,
    ending_content TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE
);

-- 剧本建筑/场景表
CREATE TABLE IF NOT EXISTS script_architecture (
    arch_id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER NOT NULL,
    arch_name TEXT,
    image_url TEXT,
    arch_desc TEXT,
    FOREIGN KEY (script_id) REFERENCES script(script_id) ON DELETE CASCADE
);

-- 游戏进度表
CREATE TABLE IF NOT EXISTS game_progress (
    game_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    script_id INTEGER NOT NULL,
    user_role_id INTEGER,
    current_act_id INTEGER,
    phase TEXT DEFAULT 'BACKGROUND',
    status TEXT DEFAULT 'PLAYING',
    voted_role_id INTEGER,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    FOREIGN KEY (script_id) REFERENCES script(script_id),
    FOREIGN KEY (user_role_id) REFERENCES role(role_id),
    FOREIGN KEY (current_act_id) REFERENCES act(act_id)
);

-- 已解锁线索表
CREATE TABLE IF NOT EXISTS game_unlocked_clue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    clue_id INTEGER NOT NULL,
    unlock_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES game_progress(game_id) ON DELETE CASCADE,
    FOREIGN KEY (clue_id) REFERENCES script_clue(clue_id) ON DELETE CASCADE,
    UNIQUE(game_id, clue_id)
);

-- 聊天记录表
CREATE TABLE IF NOT EXISTS game_chat_record (
    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    act_id INTEGER NOT NULL,
    sender_role_id INTEGER,
    content TEXT NOT NULL,
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES game_progress(game_id) ON DELETE CASCADE,
    FOREIGN KEY (act_id) REFERENCES act(act_id) ON DELETE CASCADE
);

-- 搜证动作配置表
CREATE TABLE IF NOT EXISTS investigate_action (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    script_id INTEGER DEFAULT 0,
    action_name TEXT NOT NULL,
    no_clue_reply TEXT,
    sort_order INTEGER DEFAULT 0
);

-- 用户游玩记录表
CREATE TABLE IF NOT EXISTS user_play_record (
    record_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    script_id INTEGER NOT NULL,
    script_name TEXT,
    play_duration INTEGER,
    remark TEXT,
    play_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
);

-- 用户浏览记录表
CREATE TABLE IF NOT EXISTS user_browse_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_id INTEGER NOT NULL,
    target_type INTEGER DEFAULT 1,
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户评价表
CREATE TABLE IF NOT EXISTS user_evaluate_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    script_id INTEGER NOT NULL,
    script_name TEXT,
    score INTEGER,
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
);

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorite (
    user_id INTEGER NOT NULL,
    script_id INTEGER NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, script_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_script_user ON script(user_id);
CREATE INDEX IF NOT EXISTS idx_script_series ON script(series_id);
CREATE INDEX IF NOT EXISTS idx_role_script ON role(script_id);
CREATE INDEX IF NOT EXISTS idx_act_script ON act(script_id);
CREATE INDEX IF NOT EXISTS idx_role_act ON role_act_content(script_id, act_id);
CREATE INDEX IF NOT EXISTS idx_clue_script ON script_clue(script_id);
CREATE INDEX IF NOT EXISTS idx_ending_script ON script_ending(script_id);
CREATE INDEX IF NOT EXISTS idx_arch_script ON script_architecture(script_id);
CREATE INDEX IF NOT EXISTS idx_game_user ON game_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_game_script ON game_progress(script_id);
CREATE INDEX IF NOT EXISTS idx_chat_game ON game_chat_record(game_id);
CREATE INDEX IF NOT EXISTS idx_play_user ON user_play_record(user_id);
CREATE INDEX IF NOT EXISTS idx_eval_user ON user_evaluate_record(user_id);
CREATE INDEX IF NOT EXISTS idx_eval_script ON user_evaluate_record(script_id);
