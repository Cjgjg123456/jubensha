package org.example.jubensha.entity;

public class Script {
    private Integer scriptId;
    private String title;
    private String intro;
    private Integer playerCount;
    private String difficulty;
    private String coverUrl;
    private String tags;
    private String truthContent;
    private Double score;
    private Long userId;
    private String creatorName;

    public Integer getScriptId() { return scriptId; }
    public void setScriptId(Integer scriptId) { this.scriptId = scriptId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public Integer getPlayerCount() { return playerCount; }
    public void setPlayerCount(Integer playerCount) { this.playerCount = playerCount; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getTruthContent() { return truthContent; }
    public void setTruthContent(String truthContent) { this.truthContent = truthContent; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    // 在现有的 private Long userId; 下方添加：
    private Integer seriesId;      // 所属系列ID
    private Integer seriesOrder;   // 在系列中的关卡序号
    private String seriesName;     // 所属系列名称（用于前端展示）

    // 补充对应的 Getter 和 Setter
    public Integer getSeriesId() { return seriesId; }
    public void setSeriesId(Integer seriesId) { this.seriesId = seriesId; }

    public Integer getSeriesOrder() { return seriesOrder; }
    public void setSeriesOrder(Integer seriesOrder) { this.seriesOrder = seriesOrder; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    // 在现有的字段下方添加：
    private String bgmUrl;

    public String getBgmUrl() { return bgmUrl; }
    public void setBgmUrl(String bgmUrl) { this.bgmUrl = bgmUrl; }
}