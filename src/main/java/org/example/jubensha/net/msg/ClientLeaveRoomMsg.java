package org.example.jubensha.net.msg;

public class ClientLeaveRoomMsg extends BaseMsg {
    private String userId;
    private String roomId;

    public ClientLeaveRoomMsg() {
        super("LEAVE_ROOM");
    }

    public ClientLeaveRoomMsg(String userId, String roomId) {
        super("LEAVE_ROOM");
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