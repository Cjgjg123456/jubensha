package org.example.jubensha.mapper;

import org.example.jubensha.entity.User;
import org.example.jubensha.entity.UserRegistrationHistory;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    User selectByPhone(String phone);

    @Insert("INSERT INTO sys_user (username, password, phone, gender, hobby_type, user_level, uid, nickname, real_name, birthday, city, profile, avatar_url, is_deleted) " +
            "VALUES (#{username}, #{password}, #{phone}, #{gender}, #{hobbyType}, #{userLevel}, #{uid}, #{nickname}, #{realName}, #{birthday}, #{city}, #{profile}, #{avatarUrl}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insertUser(User user);

    @Update("UPDATE sys_user SET nickname=#{nickname}, real_name=#{realName}, gender=#{gender}, hobby_type=#{hobbyType}, " +
            "birthday=#{birthday}, city=#{city}, profile=#{profile}, avatar_url=#{avatarUrl}, update_time=CURRENT_TIMESTAMP WHERE user_id=#{userId}")
    int updateUserProfile(User user);

    @Select("SELECT * FROM user_registration_history WHERE user_id = #{userId}")
    UserRegistrationHistory getUserRegistrationHistory(Long userId);

    @Select("SELECT r.*, s.cover_url FROM user_play_record r LEFT JOIN script s ON r.script_id = s.script_id WHERE r.user_id = #{userId} AND r.is_deleted = 0 ORDER BY r.create_time DESC")
    List<Map<String, Object>> getPlayRecords(Long userId);

    @Select("SELECT f.*, u.nickname, u.username, u.avatar_url, u.profile FROM user_follow f JOIN sys_user u ON f.followed_user_id = u.user_id WHERE f.user_id = #{userId} AND f.is_deleted = 0 ORDER BY f.follow_time DESC")
    List<Map<String, Object>> getFollows(Long userId);

    @Select("SELECT b.*, s.title, s.cover_url, s.tags FROM user_browse_history b LEFT JOIN script s ON b.target_id = s.script_id AND b.target_type = 1 WHERE b.user_id = #{userId} AND b.is_deleted = 0 ORDER BY b.browse_time DESC")
    List<Map<String, Object>> getBrowseHistory(Long userId);

    @Select("SELECT c.*, s.cover_url FROM user_evaluate_record c LEFT JOIN script s ON c.script_id = s.script_id WHERE c.user_id = #{userId} AND c.is_deleted = 0 ORDER BY c.evaluate_time DESC")
    List<Map<String, Object>> getComments(Long userId);

    // ================= 核心修复：只查当前用户的剧本 =================
    @Select("SELECT * FROM script WHERE user_id = #{userId} ORDER BY script_id DESC")
    List<Map<String, Object>> getCreations(Long userId);

    @Select("SELECT COUNT(*) FROM user_play_record WHERE user_id = #{userId} AND is_deleted = 0")
    int countPlayRecords(Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE user_id = #{userId} AND is_deleted = 0")
    int countFollows(Long userId);

    @Select("SELECT COUNT(*) FROM user_browse_history WHERE user_id = #{userId} AND is_deleted = 0")
    int countBrowseHistory(Long userId);

    @Select("SELECT COUNT(*) FROM user_evaluate_record WHERE user_id = #{userId} AND is_deleted = 0")
    int countComments(Long userId);

    // ================= 核心修复：只统计当前用户的剧本数量 =================
    @Select("SELECT COUNT(*) FROM script WHERE user_id = #{userId}")
    int countCreations(Long userId);

    @Update("UPDATE user_evaluate_record SET is_deleted = 1 WHERE evaluate_id = #{evaluateId}")
    int deleteComment(@Param("evaluateId") Long evaluateId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE user_id = #{userId} AND followed_user_id = #{followedUserId} AND is_deleted = 0")
    int checkFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}")
    int checkFollowAny(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    @Insert("INSERT INTO user_follow (user_id, followed_user_id, follow_time, is_deleted) VALUES (#{userId}, #{followedUserId}, CURRENT_TIMESTAMP, 0)")
    void insertFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    @Update("UPDATE user_follow SET is_deleted = 0, follow_time = CURRENT_TIMESTAMP WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}")
    void reFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    @Delete("DELETE FROM user_follow WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}")
    void deleteFollow(@Param("userId") Long userId, @Param("followedUserId") Long followedUserId);

    @Select("SELECT followed_user_id FROM user_follow WHERE user_id = #{userId} AND is_deleted = 0")
    List<Long> getFollowedUserIds(Long userId);
}