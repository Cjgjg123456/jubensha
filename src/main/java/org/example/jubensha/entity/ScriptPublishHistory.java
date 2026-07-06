package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScriptPublishHistory {
    private Integer id;
    private Integer scriptId;
    private Long userId;
    private String title;
    private LocalDateTime publishTime;
    private LocalDateTime lastModifiedTime;
    private String status;
    private Integer viewCount;
    private Integer playCount;
    private Integer favoriteCount;
}
