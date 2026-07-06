package org.example.jubensha.net.msg;

public class ServerPlayerJoinMsg extends BaseMsg {
    private String roomId;
    private String userId;
    private String username;
    private String avatar;

    public ServerPlayerJoinMsg() {
        super("PLAYER_JOIN");
    }

    public ServerPlayerJoinMsg(String roomId, String userId, String username, String avatar) {
        super("PLAYER_JOIN");
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
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

    @Override
    public void doBiz() {
    }
}