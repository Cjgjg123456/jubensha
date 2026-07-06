package org.example.jubensha.net.msg;

public class ServerPlayerLeaveMsg extends BaseMsg {
    private String roomId;
    private String userId;

    public ServerPlayerLeaveMsg() {
        super("PLAYER_LEAVE");
    }

    public ServerPlayerLeaveMsg(String roomId, String userId) {
        super("PLAYER_LEAVE");
        this.roomId = roomId;
        this.userId = userId;
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

    @Override
    public void doBiz() {
    }
}