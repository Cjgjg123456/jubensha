package org.example.jubensha.net.msg;

public class ClientShareClueMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private String clueId;
    private String clueName;
    private String clueDescription;

    public ClientShareClueMsg() {
        super("SHARE_CLUE");
    }

    public ClientShareClueMsg(String userId, String roomId, String clueId, String clueName, String clueDescription) {
        super("SHARE_CLUE");
        this.userId = userId;
        this.roomId = roomId;
        this.clueId = clueId;
        this.clueName = clueName;
        this.clueDescription = clueDescription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getClueId() {
        return clueId;
    }

    public void setClueId(String clueId) {
        this.clueId = clueId;
    }

    public String getClueName() {
        return clueName;
    }

    public void setClueName(String clueName) {
        this.clueName = clueName;
    }

    public String getClueDescription() {
        return clueDescription;
    }

    public void setClueDescription(String clueDescription) {
        this.clueDescription = clueDescription;
    }

    @Override
    public void doBiz() {
    }
}