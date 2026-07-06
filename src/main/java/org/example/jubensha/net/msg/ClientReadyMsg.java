package org.example.jubensha.net.msg;

public class ClientReadyMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private boolean ready;

    public ClientReadyMsg() {
        super("READY");
    }

    public ClientReadyMsg(String userId, String roomId, boolean ready) {
        super("READY");
        this.userId = userId;
        this.roomId = roomId;
        this.ready = ready;
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

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public void doBiz() {
    }
}