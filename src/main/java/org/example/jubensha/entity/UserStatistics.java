package org.example.jubensha.entity;

import lombok.Data;

/**
 * 用户各类数据统计
 */
@Data
public class UserStatistics {
    private Integer playRecordCount;  // 玩本记录数
    private Integer followCount;      // 历史关注数
    private Integer historyCount;     // 历史记录数
    private Integer commentCount;     // 评价记录数
    private Integer creationCount;    // 创作数
}