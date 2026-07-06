package org.example.jubensha.net.msg;

public class ServerLoginResultMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String userId;
    private String username;

    public ServerLoginResultMsg() {
        super("LOGIN_RESULT");
    }

    public ServerLoginResultMsg(boolean success, String message) {
        super("LOGIN_RESULT");
        this.success = success;
        this.message = message;
    }

    public ServerLoginResultMsg(boolean success, String message, String userId, String username) {
        super("LOGIN_RESULT");
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
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

    @Override
    public void doBiz() {
    }
}