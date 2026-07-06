package org.example.jubensha.net.msg;

public class ClientAccuseMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private String targetUserId;
    private String targetUsername;
    private String evidence;
    private String reasoning;

    public ClientAccuseMsg() {
        super("ACCUSE");
    }

    public ClientAccuseMsg(String userId, String roomId, String targetUserId, String targetUsername, String evidence, String reasoning) {
        super("ACCUSE");
        this.userId = userId;
        this.roomId = roomId;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.evidence = evidence;
        this.reasoning = reasoning;
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