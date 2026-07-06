package org.example.jubensha.net.msg;

import java.util.List;
import java.util.Map;

public class ServerStartGameMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String roomId;
    private List<Map<String, Object>> messages;  // 游戏开始前的聊天记录

    public ServerStartGameMsg() {
        super("START_GAME_RESULT");
    }

    public ServerStartGameMsg(boolean success, String message) {
        super("START_GAME_RESULT");
        this.success = success;
        this.message = message;
    }

    public ServerStartGameMsg(boolean success, String message, String roomId) {
        super("START_GAME_RESULT");
        this.success = success;
        this.message = message;
        this.roomId = roomId;
    }

    public ServerStartGameMsg(boolean success, String message, String roomId, List<Map<String, Object>> messages) {
        super("START_GAME_RESULT");
        this.success = success;
        this.message = message;
        this.roomId = roomId;
        this.messages = messages;
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

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    @Override
    public void doBiz() {
    }
}