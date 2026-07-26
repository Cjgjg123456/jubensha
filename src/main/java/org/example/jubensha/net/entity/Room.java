package org.example.jubensha.net.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    public static final String STATUS_IDLE = "IDLE";
    public static final String STATUS_PLAYING = "PLAYING";
    public static final String STATUS_FINISHED = "FINISHED";

    private String roomId;
    private String roomName;
    private String scriptId;
    private String hostId;
    private String status;
    private int maxPlayers;
    private List<Player> players;
    private long createTime;
    private List<Map<String, Object>> messages;
    private Integer gameId;

    public Room() {
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.messages = Collections.synchronizedList(new ArrayList<>());
        this.status = STATUS_IDLE;
        this.maxPlayers = 6;
        this.createTime = System.currentTimeMillis();
        // ✅ 在创建房间时生成固定的 gameId（使用时间戳确保唯一性）
        this.gameId = (int)(System.currentTimeMillis() % 100000);
    }

    public Room(String roomId, String roomName, String scriptId, String hostId) {
        this();
        this.roomId = roomId;
        this.roomName = roomName;
        this.scriptId = scriptId;
        this.hostId = hostId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public List<Player> getPlayers() {
        synchronized (players) {
            return new ArrayList<>(players);
        }
    }

    public void setPlayers(List<Player> players) {
        this.players = Collections.synchronizedList(new ArrayList<>(players));
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isFull() {
        synchronized (players) {
            return players.size() >= maxPlayers;
        }
    }

    public boolean hasRealPlayers() {
        synchronized (players) {
            return players.stream().anyMatch(p -> {
                String uid = p.getUserId();
                return uid != null && !uid.startsWith("ai_");
            });
        }
    }

    public boolean addPlayer(Player player) {
        if (player == null || player.getUserId() == null) {
            return false;
        }
        synchronized (players) {
            if (players.stream().anyMatch(p -> player.getUserId().equals(p.getUserId()))) {
                return false;
            }
            if (players.size() >= maxPlayers) {
                return false;
            }
            return players.add(player);
        }
    }

    public boolean removePlayer(String userId) {
        synchronized (players) {
            return players.removeIf(p -> p.getUserId().equals(userId));
        }
    }

    public Player getPlayer(String userId) {
        synchronized (players) {
            return players.stream().filter(p -> p.getUserId().equals(userId)).findFirst().orElse(null);
        }
    }

    public int getPlayerCount() {
        synchronized (players) {
            return players.size();
        }
    }

    public List<Map<String, Object>> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = Collections.synchronizedList(new ArrayList<>(messages));
    }

    public void addMessage(String userId, String username, String avatar, String content) {
        addMessage(userId, username, avatar, content, null);
    }

    public void addMessage(String userId, String username, String avatar, String content, String msgId) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", msgId != null && !msgId.isBlank() ? msgId : java.util.UUID.randomUUID().toString());
        message.put("msgId", message.get("id"));
        message.put("userId", userId);
        message.put("username", username);
        message.put("avatar", avatar);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());
        this.messages.add(message);
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
}