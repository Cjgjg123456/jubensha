package org.example.jubensha.net;

import org.example.jubensha.net.entity.AIPlayer;
import org.example.jubensha.net.entity.Room;
import org.example.jubensha.net.msg.ServerChatMsg;
import org.example.jubensha.net.msg.ServerPlayerJoinMsg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AIPlayerManager {
    private static AIPlayerManager instance;
    private final Map<String, AIPlayer> aiPlayers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    private AIPlayerManager() {}
    
    public static AIPlayerManager getInstance() {
        if (instance == null) {
            instance = new AIPlayerManager();
        }
        return instance;
    }
    
    public void fillRoomWithAI(Room room) {
        int currentPlayers = room.getPlayerCount();
        int maxPlayers = room.getMaxPlayers();
        
        if (currentPlayers >= maxPlayers) {
            return;
        }
        
        int aiCount = maxPlayers - currentPlayers;
        System.out.println("房间 " + room.getRoomId() + " 填充 " + aiCount + " 个AI玩家");
        
        for (int i = 0; i < aiCount; i++) {
            AIPlayer ai = new AIPlayer(room.getRoomId());
            room.addPlayer(ai);
            aiPlayers.put(ai.getUserId(), ai);
            
            ServerPlayerJoinMsg joinMsg = new ServerPlayerJoinMsg(
                room.getRoomId(), ai.getUserId(), ai.getUsername(), ai.getAvatar());
            GameServer.getInstance().broadcastToRoom(room.getRoomId(), joinMsg);
            
            startAIChatting(ai, room);
        }
    }
    
    public void fillRoomWithAI(Room room, int targetPlayerCount) {
        int currentPlayers = room.getPlayerCount();
        
        if (currentPlayers >= targetPlayerCount) {
            return;
        }
        
        int aiCount = targetPlayerCount - currentPlayers;
        if (aiCount > room.getMaxPlayers() - currentPlayers) {
            aiCount = room.getMaxPlayers() - currentPlayers;
        }
        
        System.out.println("房间 " + room.getRoomId() + " 填充 " + aiCount + " 个AI玩家（目标: " + targetPlayerCount + "）");
        
        for (int i = 0; i < aiCount; i++) {
            AIPlayer ai = new AIPlayer(room.getRoomId());
            room.addPlayer(ai);
            aiPlayers.put(ai.getUserId(), ai);
            
            ServerPlayerJoinMsg joinMsg = new ServerPlayerJoinMsg(
                room.getRoomId(), ai.getUserId(), ai.getUsername(), ai.getAvatar());
            GameServer.getInstance().broadcastToRoom(room.getRoomId(), joinMsg);
            
            startAIChatting(ai, room);
        }
    }
    
    private void startAIChatting(AIPlayer ai, Room room) {
        scheduler.scheduleWithFixedDelay(() -> {
            if (!aiPlayers.containsKey(ai.getUserId())) {
                return;
            }

            if (ai.shouldChat()) {
                String message = ai.generateChatMessage();
                ServerChatMsg chatMsg = new ServerChatMsg(
                    room.getRoomId(), ai.getUserId(), ai.getUsername(), ai.getAvatar(), message);
                GameServer.getInstance().broadcastToRoom(room.getRoomId(), chatMsg);

                room.addMessage(ai.getUserId(), ai.getUsername(), ai.getAvatar(), message);

                System.out.println("AI " + ai.getUsername() + " 发送消息: " + message);
            }
        }, ai.getChatInterval(), ai.getChatInterval(), TimeUnit.MILLISECONDS);
    }
    
    public void removeAIPlayer(String userId) {
        aiPlayers.remove(userId);
    }
    
    public void removeRoomAIPlayers(String roomId) {
        aiPlayers.entrySet().removeIf(entry -> entry.getValue().getRoomId().equals(roomId));
    }
    
    public int getAIPlayerCount() {
        return aiPlayers.size();
    }
    
    public boolean isAIPlayer(String userId) {
        return userId != null && userId.startsWith("ai_");
    }
}