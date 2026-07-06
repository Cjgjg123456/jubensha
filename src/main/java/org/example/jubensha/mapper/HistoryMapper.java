package org.example.jubensha.mapper;

import org.apache.ibatis.annotations.*;
import org.example.jubensha.entity.ScriptPublishHistory;
import org.example.jubensha.entity.UserRegistrationHistory;

import java.util.List;

@Mapper
public interface HistoryMapper {

    @Insert("INSERT INTO user_registration_history (user_id, username, phone, nickname, ip_address, device_info, status) " +
            "VALUES (#{userId}, #{username}, #{phone}, #{nickname}, #{ipAddress}, #{deviceInfo}, 'active')")
    void insertUserRegistration(UserRegistrationHistory history);

    @Select("SELECT * FROM user_registration_history WHERE user_id = #{userId}")
    UserRegistrationHistory getUserRegistrationByUserId(Long userId);

    @Select("SELECT * FROM user_registration_history ORDER BY registration_time DESC")
    List<UserRegistrationHistory> getAllUserRegistrations();

    @Update("UPDATE user_registration_history SET status = #{status} WHERE user_id = #{userId}")
    void updateUserRegistrationStatus(@Param("userId") Long userId, @Param("status") String status);

    @Insert("INSERT INTO script_publish_history (script_id, user_id, title, status) " +
            "VALUES (#{scriptId}, #{userId}, #{title}, 'published')")
    void insertScriptPublish(ScriptPublishHistory history);

    @Select("SELECT * FROM script_publish_history WHERE user_id = #{userId} ORDER BY publish_time DESC")
    List<ScriptPublishHistory> getScriptPublishByUserId(Long userId);

    @Select("SELECT * FROM script_publish_history WHERE script_id = #{scriptId}")
    ScriptPublishHistory getScriptPublishByScriptId(Integer scriptId);

    @Select("SELECT * FROM script_publish_history ORDER BY publish_time DESC")
    List<ScriptPublishHistory> getAllScriptPublishes();

    @Update("UPDATE script_publish_history SET view_count = view_count + 1 WHERE script_id = #{scriptId}")
    void incrementViewCount(Integer scriptId);

    @Update("UPDATE script_publish_history SET play_count = play_count + 1 WHERE script_id = #{scriptId}")
    void incrementPlayCount(Integer scriptId);

    @Update("UPDATE script_publish_history SET favorite_count = favorite_count + 1 WHERE script_id = #{scriptId}")
    void incrementFavoriteCount(Integer scriptId);

    @Update("UPDATE script_publish_history SET favorite_count = favorite_count - 1 WHERE script_id = #{scriptId} AND favorite_count > 0")
    void decrementFavoriteCount(Integer scriptId);

    @Update("UPDATE script_publish_history SET last_modified_time = CURRENT_TIMESTAMP WHERE script_id = #{scriptId}")
    void updateScriptModifiedTime(Integer scriptId);
}
