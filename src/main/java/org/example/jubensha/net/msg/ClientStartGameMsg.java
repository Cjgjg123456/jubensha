package org.example.jubensha.net.msg;

public class ClientStartGameMsg extends BaseMsg {
    private String userId;
    private String roomId;

    public ClientStartGameMsg() {
        super("START_GAME");
    }

    public ClientStartGameMsg(String userId, String roomId) {
        super("START_GAME");
        this.userId = userId;
        this.roomId = roomId;
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

    @Override
    public void doBiz() {
    }
}