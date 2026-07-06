package org.example.jubensha.net.msg;

public class ServerCreateRoomResultMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String roomId;

    public ServerCreateRoomResultMsg() {
        super("CREATE_ROOM_RESULT");
    }

    public ServerCreateRoomResultMsg(boolean success, String message) {
        super("CREATE_ROOM_RESULT");
        this.success = success;
        this.message = message;
    }

    public ServerCreateRoomResultMsg(boolean success, String message, String roomId) {
        super("CREATE_ROOM_RESULT");
        this.success = success;
        this.message = message;
        this.roomId = roomId;
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

    @Override
    public void doBiz() {
    }
}