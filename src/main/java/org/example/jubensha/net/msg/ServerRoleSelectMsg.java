package org.example.jubensha.net.msg;

public class ServerRoleSelectMsg extends BaseMsg {
    private boolean success;
    private String message;
    private String roomId;
    private String userId;
    private String roleId;
    private String roleName;

    public ServerRoleSelectMsg() {
        super("ROLE_SELECT");
    }

    public ServerRoleSelectMsg(boolean success, String message) {
        super("ROLE_SELECT");
        this.success = success;
        this.message = message;
    }

    public ServerRoleSelectMsg(boolean success, String message, String roomId, String userId, String roleId, String roleName) {
        super("ROLE_SELECT");
        this.success = success;
        this.message = message;
        this.roomId = roomId;
        this.userId = userId;
        this.roleId = roleId;
        this.roleName = roleName;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void doBiz() {
    }
}