package org.example.jubensha.net.msg;

public class ServerReadyResultMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String userId;
    private boolean ready;

    public ServerReadyResultMsg() {
        super("READY_RESULT");
    }

    public ServerReadyResultMsg(boolean success, String message) {
        super("READY_RESULT");
        this.success = success;
        this.message = message;
    }

    public ServerReadyResultMsg(boolean success, String message, String userId, boolean ready) {
        super("READY_RESULT");
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.ready = ready;
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