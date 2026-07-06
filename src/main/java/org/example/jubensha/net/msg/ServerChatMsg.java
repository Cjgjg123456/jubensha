package org.example.jubensha.net.msg;

public class ServerChatMsg extends BaseMsg {
    private String roomId;
    private String userId;
    private String username;
    private String avatar;
    private String content;

    public ServerChatMsg() {
        super("SERVER_CHAT");
    }

    public ServerChatMsg(String roomId, String userId, String username, String avatar, String content) {
        super("SERVER_CHAT");
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.content = content;
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