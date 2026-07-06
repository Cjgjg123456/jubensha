package org.example.jubensha.net.msg;

public class ServerAccuseMsg extends BaseMsg {
    private String roomId;
    private String userId;
    private String username;
    private String targetUserId;
    private String targetUsername;
    private String evidence;
    private String reasoning;

    public ServerAccuseMsg() {
        super("ACCUSE_RESULT");
    }

    public ServerAccuseMsg(String roomId, String userId, String username,
                          String targetUserId, String targetUsername,
                          String evidence, String reasoning) {
        super("ACCUSE_RESULT");
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.evidence = evidence;
        this.reasoning = reasoning;
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

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    @Override
    public void doBiz() {
    }
}