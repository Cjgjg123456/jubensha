package org.example.jubensha.entity;

public class ScriptArchitecture {
    private Integer archId;
    private Integer scriptId;
    private String archName;
    private String imageUrl;
    private String modelUrl;
    private String archDesc;

    // Getter 和 Setter
    public Integer getArchId() { return archId; }
    public void setArchId(Integer archId) { this.archId = archId; }
    public Integer getScriptId() { return scriptId; }
    public void setScriptId(Integer scriptId) { this.scriptId = scriptId; }
    public String getArchName() { return archName; }
    public void setArchName(String archName) { this.archName = archName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getModelUrl() { return modelUrl; }
    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }
    public String getArchDesc() { return archDesc; }
    public void setArchDesc(String archDesc) { this.archDesc = archDesc; }
}