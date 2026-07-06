package org.example.jubensha.net.msg;

public class ClientJoinRoomMsg extends BaseMsg {
    private String userId;
    private String roomId;

    public ClientJoinRoomMsg() {
        super("JOIN_ROOM");
    }

    public ClientJoinRoomMsg(String userId, String roomId) {
        super("JOIN_ROOM");
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