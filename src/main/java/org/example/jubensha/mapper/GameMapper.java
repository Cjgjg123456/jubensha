package org.example.jubensha.mapper;

import org.apache.ibatis.annotations.*;
import org.example.jubensha.entity.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GameMapper {

    // ================== 剧本基础操作 ==================

    @Select("SELECT s.*, COALESCE(u.nickname, u.username, '暗区兵王') as creator_name FROM script s LEFT JOIN sys_user u ON CAST(s.user_id AS INTEGER) = CAST(u.user_id AS INTEGER) ORDER BY s.script_id DESC")
    @Results({
        @Result(property = "scriptId", column = "script_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "intro", column = "intro"),
        @Result(property = "playerCount", column = "player_count"),
        @Result(property = "difficulty", column = "difficulty"),
        @Result(property = "coverUrl", column = "cover_url"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "truthContent", column = "truth_content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "creatorName", column = "creator_name"),
        @Result(property = "seriesId", column = "series_id"),
        @Result(property = "seriesOrder", column = "series_order"),
        @Result(property = "bgmUrl", column = "bgm_url")
    })
    List<Script> getScriptList();

    @Select("SELECT s.*, COALESCE(u.nickname, u.username) as creator_name FROM script s LEFT JOIN sys_user u ON s.user_id = u.user_id WHERE s.user_id = #{userId} ORDER BY s.script_id DESC")
    @Results({
        @Result(property = "creatorName", column = "creator_name")
    })
    List<Script> getScriptsByUserId(Long userId);

    @Select("SELECT * FROM script WHERE script_id = #{id}")
    Script getScriptById(Integer id);

    // 🟢 包含 BGM 与系列序列号的新增
    @Insert("INSERT INTO script (title, intro, player_count, difficulty, cover_url, tags, truth_content, user_id, series_id, series_order, bgm_url) " +
            "VALUES (#{title}, #{intro}, #{playerCount}, #{difficulty}, #{coverUrl}, #{tags}, #{truthContent}, #{userId}, #{seriesId}, #{seriesOrder}, #{bgmUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "scriptId")
    void insertScript(Script script);

    // 🟢 包含 BGM 与系列序列号的更新
    @Update("UPDATE script SET title=#{title}, intro=#{intro}, player_count=#{playerCount}, difficulty=#{difficulty}, cover_url=#{coverUrl}, tags=#{tags}, truth_content=#{truthContent}, series_id=#{seriesId}, bgm_url=#{bgmUrl} WHERE script_id=#{scriptId}")
    void updateScript(Script script);

    @Delete("DELETE FROM script WHERE script_id = #{id}")
    void deleteScriptById(Integer id);

    @Select("SELECT user_id FROM sys_user WHERE username = #{username}")
    Long getUserIdByUsername(String username);

    // ================== 系列与闯关地图 ==================

    @Select("SELECT * FROM script_series ORDER BY series_id DESC")
    List<ScriptSeries> getAllSeries();

    @Select("SELECT * FROM script_series WHERE series_id = #{seriesId}")
    ScriptSeries getSeriesById(Integer seriesId);

    // 🟢 包含 background_url（大地图背景图）的新增
    @Insert("INSERT INTO script_series (series_name, series_desc, cover_url, background_url) " +
            "VALUES (#{seriesName}, #{seriesDesc}, #{coverUrl}, #{backgroundUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "seriesId")
    void insertSeries(ScriptSeries series);

    // 🟢 包含 background_url（大地图背景图）的更新
    @Update("UPDATE script_series SET series_name=#{seriesName}, series_desc=#{seriesDesc}, cover_url=#{coverUrl}, background_url=#{backgroundUrl} WHERE series_id=#{seriesId}")
    void updateSeries(ScriptSeries series);

    @Delete("DELETE FROM script_series WHERE series_id = #{seriesId}")
    void deleteSeriesById(Integer seriesId);

    @Select("SELECT MAX(series_order) FROM script WHERE series_id = #{seriesId}")
    Integer getMaxSeriesOrder(Integer seriesId);

    // RPG 闯关地图：联表查出系列下的剧本及评分
    @Select("SELECT s.*, ROUND(AVG(e.score), 1) as score, MAX(ss.series_name) as series_name " +
            "FROM script s " +
            "LEFT JOIN user_evaluate_record e ON s.script_id = e.script_id AND e.is_deleted = 0 " +
            "LEFT JOIN script_series ss ON s.series_id = ss.series_id " +
            "WHERE s.series_id = #{seriesId} " +
            "GROUP BY s.script_id " +
            "ORDER BY s.series_order ASC, s.script_id ASC")
    List<Script> getScriptsBySeriesId(Integer seriesId);

    // 查出该用户所有通关过的剧本 ID
    @Select("SELECT DISTINCT script_id FROM user_play_record WHERE user_id = #{userId} AND is_deleted = 0")
    List<Integer> getUserPlayedScriptIds(Long userId);

    // ================== 剧本组件操作 (角色/幕次/线索/结局) ==================

    @Select("SELECT * FROM role WHERE script_id = #{scriptId}")
    List<Role> getRolesByScriptId(Integer scriptId);

    @Select("SELECT * FROM role WHERE role_id = #{roleId}")
    Role getRoleById(Integer roleId);

    @Insert("INSERT INTO role (script_id, name, is_ai, avatar, background) VALUES (#{scriptId}, #{name}, #{isAi}, #{avatar}, #{background})")
    @Options(useGeneratedKeys = true, keyProperty = "roleId")
    void insertRole(Role role);

    @Select("SELECT * FROM act WHERE script_id = #{scriptId} ORDER BY sort ASC")
    List<Act> getActsByScriptId(Integer scriptId);

    @Select("SELECT * FROM act WHERE act_id = #{actId}")
    Act getActById(Integer actId);

    @Insert("INSERT INTO act (script_id, act_name, sort, public_content) VALUES (#{scriptId}, #{actName}, #{sort}, #{publicContent})")
    @Options(useGeneratedKeys = true, keyProperty = "actId")
    void insertAct(Act act);

    @Select("SELECT * FROM role_act_content WHERE script_id = #{scriptId} AND act_id = #{actId} AND role_id = #{roleId} LIMIT 1")
    RoleActContent getRoleActContent(@Param("scriptId") Integer scriptId, @Param("actId") Integer actId, @Param("roleId") Integer roleId);

    @Select("SELECT * FROM role_act_content WHERE script_id = #{scriptId}")
    List<RoleActContent> getAllContentsByScriptId(Integer scriptId);

    @Select("SELECT * FROM role_act_content WHERE act_id = #{actId} AND role_id != #{userRoleId}")
    List<RoleActContent> getAiRoleContentsByAct(@Param("actId") Integer actId, @Param("userRoleId") Integer userRoleId);

    @Insert("INSERT INTO role_act_content (script_id, act_id, role_id, content) VALUES (#{scriptId}, #{actId}, #{roleId}, #{content})")
    void insertRoleActContent(RoleActContent rac);

    @Select("SELECT * FROM script_clue WHERE script_id = #{scriptId} AND unlock_chapter_id = #{actId}")
    List<ScriptClue> getCluesByAct(@Param("scriptId") Integer scriptId, @Param("actId") Integer actId);

    @Select("SELECT * FROM script_clue WHERE script_id = #{scriptId}")
    List<ScriptClue> getAllCluesByScriptId(Integer scriptId);

    @Insert("INSERT INTO script_clue (script_id, clue_name, clue_desc, is_public, role_id, unlock_chapter_id, is_hidden, unlock_condition, scene_image_url) " +
            "VALUES (#{scriptId}, #{clueName}, #{clueDesc}, #{isPublic}, #{roleId}, #{unlockChapterId}, #{isHidden}, #{unlockCondition}, #{sceneImageUrl})")
    void insertScriptClue(ScriptClue sc);

    @Select("SELECT * FROM script_ending WHERE script_id = #{scriptId}")
    List<ScriptEnding> getEndingsByScriptId(Integer scriptId);

    @Select("SELECT * FROM script_ending WHERE script_id = #{scriptId} AND voted_role_id = #{votedRoleId}")
    ScriptEnding getEndingByVote(@Param("scriptId") Integer scriptId, @Param("votedRoleId") Integer votedRoleId);

    @Insert("INSERT INTO script_ending (script_id, voted_role_id, ending_title, ending_content) VALUES (#{scriptId}, #{votedRoleId}, #{endingTitle}, #{endingContent})")
    void insertScriptEnding(ScriptEnding ending);

    @Select("SELECT truth_content FROM script WHERE script_id = #{scriptId}")
    String getScriptTruth(Integer scriptId);

    // ================== 删除操作 ==================

    @Delete("DELETE FROM act WHERE script_id = #{scriptId}")
    void deleteActsByScriptId(Integer scriptId);

    @Delete("DELETE FROM role WHERE script_id = #{scriptId}")
    void deleteRolesByScriptId(Integer scriptId);

    @Delete("DELETE FROM role_act_content WHERE script_id = #{scriptId}")
    void deleteRoleActContentsByScriptId(Integer scriptId);

    @Delete("DELETE FROM script_clue WHERE script_id = #{scriptId}")
    void deleteCluesByScriptId(Integer scriptId);

    @Delete("DELETE FROM script_ending WHERE script_id = #{scriptId}")
    void deleteEndingsByScriptId(Integer scriptId);

    // ================== 游戏进度与交互 ==================

    @Select("SELECT * FROM game_progress WHERE game_id = #{gameId}")
    GameProgress getGameProgress(Integer gameId);

    // ✅ 新增：根据剧本ID获取所有进行中的游戏进度（用于检查角色是否已被选择）
    @Select("SELECT * FROM game_progress WHERE script_id = #{scriptId} AND status != 'FINISHED'")
    List<GameProgress> getGameProgressByScript(Integer scriptId);

    @Insert("INSERT INTO game_progress (user_id, script_id, user_role_id, current_act_id, phase) VALUES (#{userId}, #{scriptId}, #{userRoleId}, #{currentActId}, #{phase})")
    @Options(useGeneratedKeys = true, keyProperty = "gameId")
    void insertGameProgress(GameProgress p);

    @Update("UPDATE game_progress SET current_act_id=#{currentActId}, phase=#{phase}, status=#{status}, voted_role_id=#{votedRoleId} WHERE game_id=#{gameId}")
    void updateGameProgress(GameProgress progress);

    @Delete("DELETE FROM game_progress WHERE game_id = #{gameId}")
    void deleteGameProgress(Integer gameId);

    // ================== 搜证与线索记录 ==================

    @Select("SELECT c.* FROM script_clue c JOIN game_unlocked_clue u ON c.clue_id = u.clue_id WHERE u.game_id = #{gameId}")
    List<ScriptClue> getUnlockedCluesByGame(Integer gameId);

    @Insert("INSERT OR IGNORE INTO game_unlocked_clue (game_id, clue_id) VALUES (#{gameId}, #{clueId})")
    void insertUnlockedClue(@Param("gameId") Integer gameId, @Param("clueId") Long clueId);

    // 获取当前幕次下，且没有被发现的隐藏线索（供 AI 判断触发）
    @Select("SELECT * FROM script_clue WHERE script_id = #{scriptId} AND unlock_chapter_id = #{actId} AND is_hidden = 1 " +
            "AND clue_id NOT IN (SELECT clue_id FROM game_unlocked_clue WHERE game_id = #{gameId})")
    List<ScriptClue> getUndiscoveredHiddenClues(@Param("scriptId") Integer scriptId, @Param("actId") Integer actId, @Param("gameId") Integer gameId);

    // ================== 聊天记录与AI ==================

    @Insert("INSERT INTO game_chat_record (game_id, act_id, sender_role_id, content) VALUES (#{gameId}, #{actId}, #{roleId}, #{content})")
    void insertChatRecord(@Param("gameId") Integer gameId, @Param("actId") Integer actId, @Param("roleId") Integer roleId, @Param("content") String content);

    @Select("SELECT * FROM game_chat_record WHERE game_id = #{gameId} AND act_id = #{actId} ORDER BY send_time ASC")
    List<Map<String, Object>> getChatRecords(@Param("gameId") Integer gameId, @Param("actId") Integer actId);

    @Select("SELECT * FROM game_chat_record WHERE game_id = #{gameId} ORDER BY send_time DESC LIMIT 20")
    List<Map<String, Object>> getRecentGlobalChatRecords(Integer gameId);

    // ================== 用户统计与足迹 ==================

    @Insert("INSERT INTO user_play_record (user_id, script_name, script_id, play_duration, remark) VALUES (#{userId}, #{scriptName}, #{scriptId}, #{playDuration}, #{remark})")
    void insertPlayRecord(@Param("userId") Long userId, @Param("scriptName") String scriptName, @Param("scriptId") Integer scriptId, @Param("playDuration") Integer playDuration, @Param("remark") String remark);

    @Insert("INSERT INTO user_browse_history (user_id, target_id, target_type) VALUES (#{userId}, #{scriptId}, 1)")
    void insertBrowseHistory(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Select("SELECT COUNT(*) FROM user_browse_history WHERE user_id = #{userId} AND target_id = #{scriptId} AND target_type = 1 AND browse_time > datetime('now', '-5 minutes')")
    int countRecentBrowseByUser(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Update("UPDATE user_browse_history SET browse_time = CURRENT_TIMESTAMP WHERE user_id = #{userId} AND target_id = #{scriptId} AND target_type = 1 AND history_id = (SELECT MAX(history_id) FROM user_browse_history WHERE user_id = #{userId} AND target_id = #{scriptId} AND target_type = 1)")
    int updateRecentBrowseTime(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Insert("INSERT INTO user_evaluate_record (user_id, script_id, script_name, score, content) VALUES (#{userId}, #{scriptId}, #{scriptName}, #{score}, #{content})")
    void insertEvaluateRecord(@Param("userId") Long userId, @Param("scriptId") Integer scriptId, @Param("scriptName") String scriptName, @Param("score") Integer score, @Param("content") String content);

    @Select("SELECT e.*, u.nickname, u.avatar_url FROM user_evaluate_record e LEFT JOIN sys_user u ON e.user_id = u.user_id WHERE e.script_id = #{scriptId} AND e.is_deleted = 0 ORDER BY e.create_time DESC")
    List<Map<String, Object>> getEvaluationsByScriptId(Integer scriptId);

    @Select("SELECT COUNT(*) FROM user_evaluate_record WHERE script_id = #{scriptId} AND is_deleted = 0")
    int countEvaluationsByScriptId(Integer scriptId);

    @Select("SELECT ROUND(AVG(score), 1) FROM user_evaluate_record WHERE script_id = #{scriptId} AND is_deleted = 0")
    Double getAverageScore(Integer scriptId);

    // 收藏表动态生成（如果你之前用了独立建表可以忽略，保留这句兼容性强）
    @Update("CREATE TABLE IF NOT EXISTS user_favorite (user_id BIGINT, script_id INT, PRIMARY KEY(user_id, script_id))")
    void createFavoriteTableIfNotExists();

    @Select("SELECT COUNT(*) FROM user_favorite WHERE user_id = #{userId} AND script_id = #{scriptId}")
    int checkFavorite(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Insert("INSERT INTO user_favorite (user_id, script_id) VALUES (#{userId}, #{scriptId})")
    void insertFavorite(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND script_id = #{scriptId}")
    void deleteFavorite(@Param("userId") Long userId, @Param("scriptId") Integer scriptId);

    @Select("SELECT script_id FROM user_favorite WHERE user_id = #{userId}")
    List<Integer> getUserFavorites(Long userId);

    @Select("SELECT * FROM script_architecture WHERE script_id = #{scriptId}")
    List<ScriptArchitecture> getArchitecturesByScriptId(Integer scriptId);

    @Insert("INSERT INTO script_architecture (script_id, arch_name, image_url, model_url, arch_desc) VALUES (#{scriptId}, #{archName}, #{imageUrl}, #{modelUrl}, #{archDesc})")
    void insertArchitecture(ScriptArchitecture arch);

    @Delete("DELETE FROM script_architecture WHERE script_id = #{scriptId}")
    void deleteArchitecturesByScriptId(Integer scriptId);

    // 搜证动作配置表（动作与回复一一对应，按剧本区分）
    @Update("DROP TABLE IF EXISTS investigate_action")
    void dropInvestigateActionTable();

    @Update("CREATE TABLE IF NOT EXISTS investigate_action (id INTEGER PRIMARY KEY AUTOINCREMENT, script_id INTEGER DEFAULT 0, action_name TEXT NOT NULL, no_clue_reply TEXT NOT NULL, sort_order INTEGER DEFAULT 0)")
    void createInvestigateActionTable();

    @Select("SELECT action_name FROM investigate_action WHERE script_id = #{scriptId} OR script_id = 0 ORDER BY sort_order")
    List<String> getInvestigateActionsByScriptId(Integer scriptId);

    @Select("SELECT action_name, no_clue_reply FROM investigate_action WHERE script_id = #{scriptId} OR script_id = 0 ORDER BY sort_order")
    List<Map<String, String>> getInvestigateActionsWithRepliesByScriptId(Integer scriptId);

    @Insert("INSERT INTO investigate_action (script_id, action_name, no_clue_reply, sort_order) VALUES (#{scriptId}, #{actionName}, #{noClueReply}, #{sortOrder})")
    void insertInvestigateAction(@Param("scriptId") Integer scriptId, @Param("actionName") String actionName, @Param("noClueReply") String noClueReply, @Param("sortOrder") Integer sortOrder);

    @Select("SELECT no_clue_reply FROM investigate_action WHERE (script_id = #{scriptId} OR script_id = 0) AND action_name = #{actionName} ORDER BY sort_order LIMIT 1")
    String getNoClueReplyByAction(@Param("scriptId") Integer scriptId, @Param("actionName") String actionName);

    @Select("SELECT COUNT(*) FROM investigate_action WHERE script_id = #{scriptId}")
    int getInvestigateActionCountByScriptId(Integer scriptId);

    // ================== 用户相关操作 ==================

    @Select("SELECT * FROM sys_user WHERE user_id = #{userId}")
    User getUserById(Long userId);

    @Insert("INSERT INTO sys_user (username, password, phone, gender, hobby_type, user_level, uid, nickname, real_name, city, profile, is_deleted) " +
            "VALUES (#{username}, #{password}, #{phone}, #{gender}, #{hobbyType}, #{userLevel}, #{uid}, #{nickname}, #{realName}, #{city}, #{profile}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUser(User user);

    @Select("SELECT COUNT(*) FROM sys_user")
    int getUserCount();

    // ================== 数据初始化方法 ==================

    @Select("SELECT COUNT(*) FROM script")
    Integer countScripts();

    @Insert("INSERT INTO script (script_id, title, intro, player_count, difficulty, cover_url, tags, truth_content, user_id, series_id, series_order, bgm_url) " +
            "VALUES (#{scriptId}, #{title}, #{intro}, #{playerCount}, #{difficulty}, #{coverUrl}, #{tags}, #{truthContent}, #{userId}, #{seriesId}, #{seriesOrder}, #{bgmUrl})")
    void insertScriptWithId(@Param("scriptId") Integer scriptId, @Param("title") String title, @Param("intro") String intro, 
                            @Param("playerCount") Integer playerCount, @Param("difficulty") String difficulty, 
                            @Param("coverUrl") String coverUrl, @Param("tags") String tags, @Param("truthContent") String truthContent,
                            @Param("userId") Integer userId, @Param("seriesId") Integer seriesId, @Param("seriesOrder") Integer seriesOrder, 
                            @Param("bgmUrl") String bgmUrl);

    @Insert("INSERT INTO script_clue (script_id, clue_name, clue_desc, is_public, role_id, unlock_chapter_id, is_hidden, unlock_condition, scene_image_url) " +
            "VALUES (#{scriptId}, #{clueName}, #{clueDesc}, #{isPublic}, #{roleId}, #{unlockChapterId}, #{isHidden}, #{unlockCondition}, #{sceneImageUrl})")
    void insertClue(@Param("scriptId") Integer scriptId, @Param("clueName") String clueName, @Param("clueDesc") String clueDesc,
                    @Param("isPublic") Integer isPublic, @Param("roleId") Integer roleId, @Param("unlockChapterId") Integer unlockChapterId,
                    @Param("isHidden") Integer isHidden, @Param("unlockCondition") String unlockCondition, @Param("sceneImageUrl") String sceneImageUrl);

    @Select("SELECT role_id FROM role WHERE script_id = #{scriptId} AND name = #{roleName}")
    Integer getRoleIdByName(@Param("scriptId") Integer scriptId, @Param("roleName") String roleName);
}