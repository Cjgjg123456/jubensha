package org.example.jubensha.net.msg;

public class ClientSelectRoleMsg extends BaseMsg {
    private String userId;
    private String roomId;
    private String roleId;
    private String roleName;

    public ClientSelectRoleMsg() {
        super("SELECT_ROLE");
    }

    public ClientSelectRoleMsg(String userId, String roomId, String roleId, String roleName) {
        super("SELECT_ROLE");
        this.userId = userId;
        this.roomId = roomId;
        this.roleId = roleId;
        this.roleName = roleName;
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