package org.example.jubensha.net.msg;

public class ClientLogoutMsg extends BaseMsg {
    private String userId;

    public ClientLogoutMsg() {
        super("LOGOUT");
    }

    public ClientLogoutMsg(String userId) {
        super("LOGOUT");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void doBiz() {
    }
}