package org.example.jubensha.entity;

import lombok.Data;

@Data
public class Role {
    private Integer roleId;
    private Integer scriptId;
    private String name;
    private Integer isAi; // 1=AI 0=玩家可选
    private String avatar;
    private String background;
}