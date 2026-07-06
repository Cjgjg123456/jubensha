package org.example.jubensha.net.msg;

import java.util.List;
import java.util.Map;

public class ServerRoomListMsg extends BaseMsg {
    private List<Map<String, Object>> rooms;

    public ServerRoomListMsg() {
        super("ROOM_LIST");
    }

    public ServerRoomListMsg(List<Map<String, Object>> rooms) {
        super("ROOM_LIST");
        this.rooms = rooms;
    }

    public List<Map<String, Object>> getRooms() {
        return rooms;
    }

    public void setRooms(List<Map<String, Object>> rooms) {
        this.rooms = rooms;
    }

    @Override
    public void doBiz() {
    }
}