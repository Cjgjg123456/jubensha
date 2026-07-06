package org.example.jubensha.net.msg;

public class ServerClueShareMsg extends BaseMsg {
    private String roomId;
    private String userId;
    private String username;
    private String avatar;
    private String clueId;
    private String clueName;
    private String clueDescription;

    public ServerClueShareMsg() {
        super("CLUE_SHARE");
    }

    public ServerClueShareMsg(String roomId, String userId, String username, String avatar,
                              String clueId, String clueName, String clueDescription) {
        super("CLUE_SHARE");
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.clueId = clueId;
        this.clueName = clueName;
        this.clueDescription = clueDescription;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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