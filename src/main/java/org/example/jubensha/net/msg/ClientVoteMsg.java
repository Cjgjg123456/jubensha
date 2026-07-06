package org.example.jubensha.net.msg;

public class ClientVoteMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private String targetUserId;
    private String targetUsername;

    public ClientVoteMsg() {
        super("VOTE");
    }

    public ClientVoteMsg(String userId, String roomId, String targetUserId, String targetUsername) {
        super("VOTE");
        this.userId = userId;
        this.roomId = roomId;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
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

    @Override
    public void doBiz() {
    }
}