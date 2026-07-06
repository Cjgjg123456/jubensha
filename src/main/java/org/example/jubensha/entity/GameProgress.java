package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameProgress {
    private Integer gameId;
    private Long userId; // 关联系统登录用户
    private Integer scriptId;
    private Integer userRoleId;
    private Integer currentActId;
    private String status; // playing, end

    /**
     * 当前游戏阶段:
     * ROLE_SELECT(选角), BACKGROUND(阅读背景),
     * ACT_READ(剧本阅读), DISCUSS(自由讨论),
     * VOTING(投票阶段), END(复盘结束)
     */
    private String phase;

    private LocalDateTime createTime;

    // ======== 新增字段 ========
    private Integer votedRoleId; // 记录玩家最终投给了哪个角色

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
}