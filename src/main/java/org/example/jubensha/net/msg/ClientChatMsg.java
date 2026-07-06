package org.example.jubensha.net.msg;

public class ClientChatMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private String content;

    public ClientChatMsg() {
        super("CHAT");
    }

    public ClientChatMsg(String userId, String roomId, String content) {
        super("CHAT");
        this.userId = userId;
        this.roomId = roomId;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void doBiz() {
    }
}