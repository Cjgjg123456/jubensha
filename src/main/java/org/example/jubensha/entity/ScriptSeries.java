package org.example.jubensha.entity;

public class ScriptSeries {
    private Integer seriesId;
    private String seriesName;
    private String seriesDesc;
    private String coverUrl;
    private String backgroundUrl; // 🟢 新增：地图背景图

    public Integer getSeriesId() { return seriesId; }
    public void setSeriesId(Integer seriesId) { this.seriesId = seriesId; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getSeriesDesc() { return seriesDesc; }
    public void setSeriesDesc(String seriesDesc) { this.seriesDesc = seriesDesc; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    // 🟢 新增的 Getter 和 Setter
    public String getBackgroundUrl() { return backgroundUrl; }
    public void setBackgroundUrl(String backgroundUrl) { this.backgroundUrl = backgroundUrl; }
}