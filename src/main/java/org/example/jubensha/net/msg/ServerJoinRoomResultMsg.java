package org.example.jubensha.net.msg;

import java.util.List;
import java.util.Map;

public class ServerJoinRoomResultMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String roomId;
    private List<Map<String, Object>> players;

    public ServerJoinRoomResultMsg() {
        super("JOIN_ROOM_RESULT");
    }

    public ServerJoinRoomResultMsg(boolean success, String message) {
        super("JOIN_ROOM_RESULT");
        this.success = success;
        this.message = message;
    }

    public ServerJoinRoomResultMsg(boolean success, String message, String roomId, List<Map<String, Object>> players) {
        super("JOIN_ROOM_RESULT");
        this.success = success;
        this.message = message;
        this.roomId = roomId;
        this.players = players;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<Map<String, Object>> getPlayers() {
        return players;
    }

    public void setPlayers(List<Map<String, Object>> players) {
        this.players = players;
    }

    @Override
    public void doBiz() {
    }
}