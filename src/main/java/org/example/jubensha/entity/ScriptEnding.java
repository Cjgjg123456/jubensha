package org.example.jubensha.entity;

public class ScriptEnding {
    private Integer endingId;
    private Integer scriptId;
    private Integer votedRoleId; // 投给该角色触发此结局 (-1为投错兜底, 0为弃票)
    private String endingTitle;
    private String endingContent;

    // Getter 和 Setter
    public Integer getEndingId() { return endingId; }
    public void setEndingId(Integer endingId) { this.endingId = endingId; }
    public Integer getScriptId() { return scriptId; }
    public void setScriptId(Integer scriptId) { this.scriptId = scriptId; }
    public Integer getVotedRoleId() { return votedRoleId; }
    public void setVotedRoleId(Integer votedRoleId) { this.votedRoleId = votedRoleId; }
    public String getEndingTitle() { return endingTitle; }
    public void setEndingTitle(String endingTitle) { this.endingTitle = endingTitle; }
    public String getEndingContent() { return endingContent; }
    public void setEndingContent(String endingContent) { this.endingContent = endingContent; }
}