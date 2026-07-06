package org.example.jubensha.entity;

public class ScriptClue {
    private Long clueId;
    private Long scriptId;
    private String locationName;
    private String clueName;
    private String clueDesc;
    private String clueImageUrl;
    private Integer isPublic;
    private Integer roleId;
    private Long unlockChapterId;

    // ============ 新增：AI 互动搜证相关字段 ============
    private Integer isHidden;          // 是否为隐藏线索(1-隐藏, 0-普通)
    private String unlockCondition;    // 触发条件(如："搜查垃圾桶")
    private String sceneImageUrl;      // 现场图片

    // Getters and Setters
    public Long getClueId() { return clueId; }
    public void setClueId(Long clueId) { this.clueId = clueId; }

    public Long getScriptId() { return scriptId; }
    public void setScriptId(Long scriptId) { this.scriptId = scriptId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getClueName() { return clueName; }
    public void setClueName(String clueName) { this.clueName = clueName; }

    public String getClueDesc() { return clueDesc; }
    public void setClueDesc(String clueDesc) { this.clueDesc = clueDesc; }

    public String getClueImageUrl() { return clueImageUrl; }
    public void setClueImageUrl(String clueImageUrl) { this.clueImageUrl = clueImageUrl; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public Long getUnlockChapterId() { return unlockChapterId; }
    public void setUnlockChapterId(Long unlockChapterId) { this.unlockChapterId = unlockChapterId; }

    public Integer getIsHidden() { return isHidden; }
    public void setIsHidden(Integer isHidden) { this.isHidden = isHidden; }

    public String getUnlockCondition() { return unlockCondition; }
    public void setUnlockCondition(String unlockCondition) { this.unlockCondition = unlockCondition; }

    public String getSceneImageUrl() { return sceneImageUrl; }
    public void setSceneImageUrl(String sceneImageUrl) { this.sceneImageUrl = sceneImageUrl; }
}