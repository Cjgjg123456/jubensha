package org.example.jubensha.net.msg;

public class ClientLoginMsg extends BaseMsg {
    private String userId;
    private String username;
    private String avatar;

    public ClientLoginMsg() {
        super("LOGIN");
    }

    public ClientLoginMsg(String userId, String username, String avatar) {
        super("LOGIN");
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
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

    @Override
    public void doBiz() {
    }
}