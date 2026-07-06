package org.example.jubensha.net.msg;

public class ClientCreateRoomMsg extends BaseMsg {
    private String userId;
    private String scriptId;
    private String roomName;
    private int playerCount;

    public ClientCreateRoomMsg() {
        super("CREATE_ROOM");
    }

    public ClientCreateRoomMsg(String userId, String scriptId, String roomName) {
        super("CREATE_ROOM");
        this.userId = userId;
        this.scriptId = scriptId;
        this.roomName = roomName;
        this.playerCount = 6;
    }

    public ClientCreateRoomMsg(String userId, String scriptId, String roomName, int playerCount) {
        super("CREATE_ROOM");
        this.userId = userId;
        this.scriptId = scriptId;
        this.roomName = roomName;
        this.playerCount = playerCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    @Override
    public void doBiz() {
    }
}