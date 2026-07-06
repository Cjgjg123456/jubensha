package org.example.jubensha.net.msg;

import java.util.Map;

public class ServerVoteMsg extends BaseMsg {
    private String roomId;
    private String userId;
    private String username;
    private String targetUserId;
    private String targetUsername;
    private Map<String, Integer> voteCounts;

    public ServerVoteMsg() {
        super("VOTE_RESULT");
    }

    public ServerVoteMsg(String roomId, String userId, String username, 
                         String targetUserId, String targetUsername) {
        super("VOTE_RESULT");
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
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

    public Map<String, Integer> getVoteCounts() {
        return voteCounts;
    }

    public void setVoteCounts(Map<String, Integer> voteCounts) {
        this.voteCounts = voteCounts;
    }

    @Override
    public void doBiz() {
    }
}