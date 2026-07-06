package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户实体类（适配sys_user表）
 */
@Data // 必须加这个注解，自动生成getter/setter
public class User {
    // 原有字段（和数据库表对应）
    private Long userId;          // 对应user_id（主键）
    private String username;      // 用户名
    private String password;      // 密码
    private String phone;         // 手机号
    private Integer gender;       // 性别（0=女，1=男）
    private String hobbyType;     // 爱好类型（对应hobby_type）
    private Integer userLevel;    // 用户等级（对应user_level）
    private String uid;           // 用户唯一标识
    private LocalDateTime createTime; // 创建时间（对应create_time）
    private LocalDateTime updateTime; // 更新时间（对应update_time）
    private Integer isDeleted;    // 逻辑删除标记（对应is_deleted）

    // 编辑资料扩展字段（和数据库表新增字段对应）
    private String nickname;      // 昵称
    private String realName;      // 真实姓名（对应real_name）
    private LocalDate birthday;   // 生日
    private String city;          // 城市
    private String profile;       // 个人简介
    private String avatarUrl;     // 头像URL（对应avatar_url）
}