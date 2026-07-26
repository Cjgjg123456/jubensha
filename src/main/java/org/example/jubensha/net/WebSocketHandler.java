package org.example.jubensha.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.jubensha.entity.GameProgress;
import org.example.jubensha.net.entity.Player;
import org.example.jubensha.net.entity.Room;
import org.example.jubensha.service.GameService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final GameServer gameServer = GameServer.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameService gameService;
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionUserIds = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionRoomIds = new ConcurrentHashMap<>();
    
    // ✅ 房间级别的锁，防止并发角色选择冲突
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();
    
    // ✅ 记录角色选择请求，用于去重和超时处理
    private final ConcurrentHashMap<String, RoleSelectionRequest> pendingRoleSelections = new ConcurrentHashMap<>();
    
    // ✅ 记录游戏开始请求的状态
    private final ConcurrentHashMap<String, GameStartRequest> pendingGameStarts = new ConcurrentHashMap<>();
    
    // ✅ 广播超时时间（毫秒）
    private static final long BROADCAST_TIMEOUT_MS = 5000;
    
    // ✅ 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    
    // ✅ 时间戳格式化器
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = "UNKNOWN";
        String remoteAddress = "UNKNOWN";
        try {
            if (session != null) {
                sessionId = session.getId();
                if (session.getRemoteAddress() != null) {
                    remoteAddress = session.getRemoteAddress().toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("sessionId", sessionId);
        logMap.put("remoteAddress", remoteAddress);
        log("INFO", "NEW_CONNECTION", "新连接建立", logMap);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = "UNKNOWN";
        try {
            if (session != null) {
                String id = session.getId();
                sessionId = id != null ? id : "UNKNOWN";
            }
        } catch (Exception e) {
            // Ignore any errors getting session ID
        }
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("sessionId", sessionId);
        logMap.put("payloadLength", payload.length());
        
        log("DEBUG", "MESSAGE_RECEIVED", "收到消息", logMap);

        try {
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");

            if (type == null) {
                log("WARN", "INVALID_MESSAGE", "消息类型为空", 
                    Map.of("sessionId", sessionId));
                return;
            }

            switch (type) {
                case "JOIN_ROOM":
                    handleJoinRoom(session, msg);
                    break;
                case "SELECT_ROLE":
                    handleSelectRole(session, msg);
                    break;
                case "START_GAME":
                    handleStartGame(session, msg);
                    break;
                case "CHAT":
                    handleChat(session, msg);
                    break;
                default:
                    log("WARN", "UNKNOWN_MESSAGE_TYPE", "未知消息类型", 
                        Map.of("type", type, "sessionId", sessionId));
            }
        } catch (Exception e) {
            log("ERROR", "MESSAGE_PARSE_ERROR", "处理消息错误: " + e.getMessage(), 
                Map.of("sessionId", sessionId, "exception", e.getClass().getName()));
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(WebSocketSession session, Map<String, Object> msg) {
        String userId = String.valueOf(msg.get("userId"));
        String username = (String) msg.get("username");
        String roomId = (String) msg.get("roomId");
        String avatar = (String) msg.getOrDefault("avatar", "");
        String sessionId = session != null ? session.getId() : "UNKNOWN";

        log("DEBUG", "JOIN_ROOM_REQUEST", "收到加入房间请求", 
            Map.of("sessionId", sessionId, "userId", userId, "username", username, "roomId", roomId));

        userSessions.put(userId, session);
        sessionUserIds.put(session, userId);
        sessionRoomIds.put(session, roomId);

        Room room = gameServer.getRoom(roomId);
        if (room != null) {
            Player existingPlayer = room.getPlayer(userId);
            if (existingPlayer == null) {
                if (!Room.STATUS_IDLE.equals(room.getStatus())) {
                    log("WARN", "JOIN_ROOM_REJECTED", "房间已开始或已结束，拒绝新玩家加入",
                        Map.of("userId", userId, "username", username, "roomId", roomId, "status", room.getStatus()));
                    return;
                }

                Player roomPlayer = new Player(userId, username, avatar);
                roomPlayer.setRoomId(roomId);
                if (!room.addPlayer(roomPlayer)) {
                    log("WARN", "JOIN_ROOM_REJECTED", "房间已满或用户重复，加入失败",
                        Map.of("userId", userId, "username", username, "roomId", roomId,
                               "playerCount", room.getPlayerCount(), "maxPlayers", room.getMaxPlayers()));
                    return;
                }
                log("INFO", "JOIN_ROOM", "用户加入房间",
                    Map.of("userId", userId, "username", username, "roomId", roomId,
                           "status", room.getStatus(), "gameId", room.getGameId(),
                           "playerCount", room.getPlayerCount(), "maxPlayers", room.getMaxPlayers()));
            } else {
                log("INFO", "JOIN_ROOM", "用户已在房间中",
                    Map.of("userId", userId, "username", username, "roomId", roomId,
                           "status", room.getStatus(), "gameId", room.getGameId()));
            }
            
            // 打印房间内所有玩家信息
            log("DEBUG", "JOIN_ROOM_PLAYERS", "房间当前玩家列表", 
                Map.of("roomId", roomId, "players", room.getPlayers().stream()
                    .map(p -> p.getUsername() + "(" + p.getUserId() + ")")
                    .toList()));
        } else {
            log("WARN", "JOIN_ROOM", "房间不存在", 
                Map.of("roomId", roomId));
        }
    }

    private void handleSelectRole(WebSocketSession session, Map<String, Object> msg) {
        String userId = String.valueOf(msg.getOrDefault("userId", "UNKNOWN"));
        String roomId = (String) msg.getOrDefault("roomId", "UNKNOWN");
        String roleIdStr = String.valueOf(msg.getOrDefault("roleId", "UNKNOWN"));
        String roleName = (String) msg.getOrDefault("roleName", "UNKNOWN");
        String sessionId = "UNKNOWN";
        try {
            if (session != null) {
                String id = session.getId();
                sessionId = id != null ? id : "UNKNOWN";
            }
        } catch (Exception e) {
            // Ignore any errors getting session ID
        }
        
        // ✅ 生成请求ID用于追踪
        String requestId = generateRequestId();
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("requestId", requestId);
        logMap.put("userId", userId);
        logMap.put("roomId", roomId);
        logMap.put("roleId", roleIdStr);
        logMap.put("roleName", roleName);
        logMap.put("sessionId", sessionId);
        
        log("INFO", "SELECT_ROLE_REQUEST", "收到角色选择请求", logMap);

        // ✅ 获取房间锁，防止并发冲突
        ReentrantLock roomLock = roomLocks.computeIfAbsent(roomId, k -> new ReentrantLock());
        
        try {
            // ✅ 尝试获取锁，带超时
            long lockStartTime = System.currentTimeMillis();
            Map<String, Object> logMap1 = new LinkedHashMap<>();
            logMap1.put("requestId", requestId);
            logMap1.put("roomId", roomId);
            logMap1.put("userId", userId);
            logMap1.put("queueLength", roomLock.getQueueLength());
            logMap1.put("isLocked", roomLock.isLocked());
            logMap1.put("lockStartTime", lockStartTime);
            log("DEBUG", "SELECT_ROLE_WAITING_FOR_LOCK", "正在等待房间锁", logMap1);
            
            boolean locked = roomLock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                Map<String, Object> logMap2 = new LinkedHashMap<>();
                logMap2.put("requestId", requestId);
                logMap2.put("roomId", roomId);
                logMap2.put("userId", userId);
                logMap2.put("waitTime", System.currentTimeMillis() - lockStartTime);
                log("WARN", "SELECT_ROLE_LOCK_TIMEOUT", "获取房间锁超时", logMap2);
                
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("type", "ROLE_SELECT_RESULT");
                responseMap.put("success", false);
                responseMap.put("message", "系统繁忙，请稍后重试");
                responseMap.put("requestId", requestId);
                sendMessage(session, responseMap);
                return;
            }

            long lockAcquiredTime = System.currentTimeMillis();
            Map<String, Object> logMap3 = new LinkedHashMap<>();
            logMap3.put("requestId", requestId);
            logMap3.put("roomId", roomId);
            logMap3.put("userId", userId);
            logMap3.put("lockWaitTime", lockAcquiredTime - lockStartTime);
            logMap3.put("queueLength", roomLock.getQueueLength());
            logMap3.put("lockAcquiredAt", lockAcquiredTime);
            log("DEBUG", "SELECT_ROLE_LOCK_ACQUIRED", "成功获取房间锁", logMap3);
            
            try {
                Room room = gameServer.getRoom(roomId);
                if (room == null) {
                    Map<String, Object> logMap4 = new LinkedHashMap<>();
                    logMap4.put("requestId", requestId);
                    logMap4.put("roomId", roomId);
                    log("ERROR", "SELECT_ROLE_ROOM_NOT_FOUND", "房间不存在", logMap4);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "ROLE_SELECT_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "房间不存在");
                    responseMap.put("requestId", requestId);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 检查角色是否已被其他玩家选择
                Map<String, Object> logMap5 = new LinkedHashMap<>();
                logMap5.put("requestId", requestId);
                logMap5.put("roomId", roomId);
                logMap5.put("userId", userId);
                logMap5.put("roleId", roleIdStr);
                log("DEBUG", "SELECT_ROLE_CHECKING_AVAILABILITY", "检查角色可用性", logMap5);
                
                Player existingPlayer = null;
                for (Player p : room.getPlayers()) {
                    if (!p.getUserId().equals(userId) && roleIdStr.equals(p.getRoleId())) {
                        existingPlayer = p;
                        break;
                    }
                }

                if (existingPlayer != null) {
                    Map<String, Object> logMap6 = new LinkedHashMap<>();
                    logMap6.put("requestId", requestId);
                    logMap6.put("roomId", roomId);
                    logMap6.put("userId", userId);
                    logMap6.put("roleId", roleIdStr);
                    logMap6.put("roleName", roleName);
                    logMap6.put("takenByUserId", existingPlayer.getUserId());
                    logMap6.put("takenByUsername", existingPlayer.getUsername());
                    log("WARN", "SELECT_ROLE_ROLE_TAKEN", "角色已被选择", logMap6);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "ROLE_SELECT_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "该角色已被 " + existingPlayer.getUsername() + " 选择");
                    responseMap.put("requestId", requestId);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 更新玩家角色
                Player player = room.getPlayer(userId);
                String previousRoleId = null;
                String previousRoleName = null;
                
                if (player != null) {
                    previousRoleId = player.getRoleId();
                    previousRoleName = player.getRoleName();
                    Map<String, Object> logMap7 = new LinkedHashMap<>();
                    logMap7.put("requestId", requestId);
                    logMap7.put("userId", userId);
                    logMap7.put("roomId", roomId);
                    logMap7.put("oldRoleId", previousRoleId != null ? previousRoleId : "none");
                    logMap7.put("oldRoleName", previousRoleName != null ? previousRoleName : "none");
                    logMap7.put("newRoleId", roleIdStr);
                    logMap7.put("newRoleName", roleName);
                    log("DEBUG", "SELECT_ROLE_UPDATING_STATE", "更新玩家角色状态", logMap7);
                    player.setRoleId(roleIdStr);
                    player.setRoleName(roleName);
                }

                Map<String, Object> logMap8 = new LinkedHashMap<>();
                logMap8.put("requestId", requestId);
                logMap8.put("userId", userId);
                logMap8.put("roomId", roomId);
                logMap8.put("roleId", roleIdStr);
                logMap8.put("roleName", roleName);
                logMap8.put("previousRoleId", previousRoleId != null ? previousRoleId : "none");
                logMap8.put("previousRoleName", previousRoleName != null ? previousRoleName : "none");
                log("INFO", "SELECT_ROLE_SUCCESS", "角色选择成功", logMap8);

                // ✅ 构建广播消息
                Map<String, Object> broadcastMsg = new LinkedHashMap<>();
                broadcastMsg.put("type", "ROLE_SELECT");
                broadcastMsg.put("requestId", requestId);
                broadcastMsg.put("timestamp", getTimestamp());
                broadcastMsg.put("userId", userId);
                broadcastMsg.put("roomId", roomId);
                broadcastMsg.put("roleId", roleIdStr);
                broadcastMsg.put("roleName", roleName);
                if (previousRoleId != null) {
                    broadcastMsg.put("previousRoleId", previousRoleId);
                }
                if (previousRoleName != null) {
                    broadcastMsg.put("previousRoleName", previousRoleName);
                }

                // ✅ 带超时和重试的广播
                long broadcastStartTime = System.currentTimeMillis();
                Map<String, Object> logMap9 = new LinkedHashMap<>();
                logMap9.put("requestId", requestId);
                logMap9.put("roomId", roomId);
                logMap9.put("broadcastStartedAt", broadcastStartTime);
                log("DEBUG", "SELECT_ROLE_STARTING_BROADCAST", "开始广播角色选择消息", logMap9);
                
                int successCount = broadcastToRoomWithRetry(roomId, broadcastMsg, requestId, "SELECT_ROLE");
                Map<String, Object> logMap10 = new LinkedHashMap<>();
                logMap10.put("requestId", requestId);
                logMap10.put("roomId", roomId);
                logMap10.put("successCount", successCount);
                logMap10.put("totalPlayers", room.getPlayerCount());
                logMap10.put("broadcastTime", System.currentTimeMillis() - broadcastStartTime);
                logMap10.put("broadcastCompletedAt", System.currentTimeMillis());
                log("DEBUG", "SELECT_ROLE_BROADCAST_COMPLETE", "广播完成", logMap10);

                // ✅ 发送成功响应
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("type", "ROLE_SELECT_RESULT");
                responseMap.put("success", true);
                responseMap.put("message", "角色选择成功");
                responseMap.put("requestId", requestId);
                responseMap.put("roleId", roleIdStr);
                responseMap.put("roleName", roleName);
                sendMessage(session, responseMap);

                // ✅ 新增：AI玩家自动选择角色逻辑
                // 当真人玩家选择角色后，检查是否需要让AI玩家自动选择剩余角色
                autoSelectRolesForAI(roomId);

            } finally {
                long lockReleaseTime = System.currentTimeMillis();
                Map<String, Object> logMap11 = new LinkedHashMap<>();
                logMap11.put("requestId", requestId);
                logMap11.put("roomId", roomId);
                logMap11.put("userId", userId);
                logMap11.put("lockReleaseTime", lockReleaseTime);
                log("DEBUG", "SELECT_ROLE_RELEASING_LOCK", "准备释放房间锁", logMap11);
                roomLock.unlock();
                
                Map<String, Object> logMap12 = new LinkedHashMap<>();
                logMap12.put("requestId", requestId);
                logMap12.put("roomId", roomId);
                logMap12.put("userId", userId);
                logMap12.put("lockHoldTime", lockReleaseTime - lockAcquiredTime);
                logMap12.put("totalLockTime", lockReleaseTime - lockStartTime);
                logMap12.put("lockReleasedAt", lockReleaseTime);
                log("DEBUG", "SELECT_ROLE_LOCK_RELEASED", "释放房间锁", logMap12);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> logMap13 = new LinkedHashMap<>();
            logMap13.put("requestId", requestId);
            logMap13.put("userId", userId);
            log("ERROR", "SELECT_ROLE_INTERRUPTED", "角色选择被中断", logMap13);
            
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("type", "ROLE_SELECT_RESULT");
            responseMap.put("success", false);
            responseMap.put("message", "操作被中断，请重试");
            responseMap.put("requestId", requestId);
            sendMessage(session, responseMap);
        }
    }

    private void handleStartGame(WebSocketSession session, Map<String, Object> msg) {
        String userId = String.valueOf(msg.getOrDefault("userId", "UNKNOWN"));
        String roomId = (String) msg.getOrDefault("roomId", "UNKNOWN");
        String sessionId = "UNKNOWN";
        try {
            if (session != null) {
                String id = session.getId();
                sessionId = id != null ? id : "UNKNOWN";
            }
        } catch (Exception e) {
            // Ignore any errors getting session ID
        }
        String requestId = generateRequestId();
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("requestId", requestId);
        logMap.put("userId", userId);
        logMap.put("roomId", roomId);
        logMap.put("sessionId", sessionId);
        
        log("INFO", "START_GAME_REQUEST", "收到开始游戏请求", logMap);

        ReentrantLock roomLock = roomLocks.computeIfAbsent(roomId, k -> new ReentrantLock());
        
        try {
            boolean locked = roomLock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                Map<String, Object> logMap1 = new LinkedHashMap<>();
                logMap1.put("requestId", requestId);
                logMap1.put("roomId", roomId);
                logMap1.put("userId", userId);
                log("WARN", "START_GAME_LOCK_TIMEOUT", "获取房间锁超时", logMap1);
                
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("type", "START_GAME_RESULT");
                responseMap.put("success", false);
                responseMap.put("message", "系统繁忙，请稍后重试");
                responseMap.put("requestId", requestId);
                sendMessage(session, responseMap);
                return;
            }

            try {
                Room room = gameServer.getRoom(roomId);
                if (room == null) {
                    Map<String, Object> logMap2 = new LinkedHashMap<>();
                    logMap2.put("requestId", requestId);
                    logMap2.put("roomId", roomId);
                    log("ERROR", "START_GAME_ROOM_NOT_FOUND", "房间不存在", logMap2);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "START_GAME_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "房间不存在");
                    responseMap.put("requestId", requestId);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 检查是否是房主
                if (!room.getHostId().equals(userId)) {
                    Map<String, Object> logMap3 = new LinkedHashMap<>();
                    logMap3.put("requestId", requestId);
                    logMap3.put("roomId", roomId);
                    logMap3.put("userId", userId);
                    logMap3.put("hostId", room.getHostId());
                    log("WARN", "START_GAME_NOT_HOST", "非房主尝试开始游戏", logMap3);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "START_GAME_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "只有房主可以开始游戏");
                    responseMap.put("requestId", requestId);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 检查房间状态
                if (!Room.STATUS_IDLE.equals(room.getStatus())) {
                    Map<String, Object> logMap4 = new LinkedHashMap<>();
                    logMap4.put("requestId", requestId);
                    logMap4.put("roomId", roomId);
                    logMap4.put("status", room.getStatus());
                    log("WARN", "START_GAME_INVALID_STATUS", "房间状态不允许开始游戏", logMap4);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "START_GAME_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "房间状态不允许开始游戏");
                    responseMap.put("requestId", requestId);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 检查所有玩家是否都已选择角色
                List<Player> playersWithoutRole = new ArrayList<>();
                for (Player p : room.getPlayers()) {
                    if (p.getRoleId() == null || p.getRoleId().isEmpty()) {
                        playersWithoutRole.add(p);
                    }
                }

                if (!playersWithoutRole.isEmpty()) {
                    List<String> missingUsernames = playersWithoutRole.stream()
                            .map(Player::getUsername)
                            .toList();
                    
                    Map<String, Object> logMap5 = new LinkedHashMap<>();
                    logMap5.put("requestId", requestId);
                    logMap5.put("roomId", roomId);
                    logMap5.put("missingPlayers", missingUsernames);
                    logMap5.put("totalPlayers", room.getPlayerCount());
                    log("WARN", "START_GAME_MISSING_ROLES", "部分玩家未选择角色", logMap5);
                    
                    Map<String, Object> responseMap = new LinkedHashMap<>();
                    responseMap.put("type", "START_GAME_RESULT");
                    responseMap.put("success", false);
                    responseMap.put("message", "请确保所有玩家都已选择角色！未选择的玩家: " + String.join(", ", missingUsernames));
                    responseMap.put("requestId", requestId);
                    responseMap.put("missingPlayers", missingUsernames);
                    sendMessage(session, responseMap);
                    return;
                }

                // ✅ 记录所有玩家的角色分配
                List<Map<String, Object>> playerRoles = new ArrayList<>();
                for (Player p : room.getPlayers()) {
                    Map<String, Object> roleInfo = new LinkedHashMap<>();
                    roleInfo.put("userId", p.getUserId());
                    roleInfo.put("username", p.getUsername());
                    roleInfo.put("roleId", p.getRoleId());
                    roleInfo.put("roleName", p.getRoleName());
                    playerRoles.add(roleInfo);
                }

                Map<String, Object> logMap6 = new LinkedHashMap<>();
                logMap6.put("requestId", requestId);
                logMap6.put("roomId", roomId);
                logMap6.put("playerRoles", playerRoles);
                logMap6.put("totalPlayers", room.getPlayerCount());
                log("INFO", "START_GAME_VALIDATION_PASSED", "开始游戏验证通过", logMap6);

                // ✅ 调用 GameService 为每个玩家创建游戏实例
                Map<String, Integer> playerGameIds = new LinkedHashMap<>();
                Integer realGameId = null;
                try {
                    // 获取剧本ID（需要从房间信息中获取）
                    Integer scriptId = null;
                    try {
                        scriptId = Integer.parseInt(room.getScriptId());
                    } catch (NumberFormatException e) {
                        log("WARN", "START_GAME_INVALID_SCRIPT", "剧本ID无效", 
                            Map.of("roomId", roomId, "scriptId", room.getScriptId()));
                        throw e;
                    }
                    
                    // 为每个玩家创建游戏进度
                    for (Player player : room.getPlayers()) {
                        // ✅ 跳过 AI 玩家（AI玩家的userId以"ai_"开头）
                        if (player.getUserId() != null && player.getUserId().startsWith("ai_")) {
                            log("DEBUG", "START_GAME_SKIP_AI", "跳过AI玩家", 
                                Map.of("userId", player.getUserId(), "username", player.getUsername(), "roomId", roomId));
                            continue;
                        }
                        
                        Long playerUserId = Long.parseLong(player.getUserId());
                        Integer playerRoleId = Integer.parseInt(player.getRoleId());
                        
                        GameProgress gameProgress = gameService.startGameForOnline(playerUserId, scriptId, playerRoleId);
                        if (gameProgress != null) {
                            playerGameIds.put(player.getUserId(), gameProgress.getGameId());
                            // 第一个玩家的 gameId 作为房间的 gameId
                            if (realGameId == null) {
                                realGameId = gameProgress.getGameId();
                            }
                            log("INFO", "START_GAME_CREATED", "为玩家创建游戏实例", 
                                Map.of("userId", playerUserId, "username", player.getUsername(), 
                                      "gameId", gameProgress.getGameId(), "roomId", roomId));
                        }
                    }
                } catch (Exception e) {
                    log("ERROR", "START_GAME_SERVICE_ERROR", "调用GameService创建游戏失败", 
                        Map.of("roomId", roomId, "error", e.getMessage()));
                    throw new RuntimeException("创建游戏失败: " + e.getMessage(), e);
                }

                // ✅ 标记房间为游戏中并设置真实游戏ID
                room.setStatus(Room.STATUS_PLAYING);
                room.setGameId(realGameId);

                // ✅ 构建广播消息，包含每个玩家的 gameId
                Map<String, Object> broadcastMsg = new LinkedHashMap<>();
                broadcastMsg.put("type", "START_GAME_RESULT");
                broadcastMsg.put("requestId", requestId);
                broadcastMsg.put("timestamp", getTimestamp());
                broadcastMsg.put("success", true);
                broadcastMsg.put("message", "游戏即将开始！");
                broadcastMsg.put("roomId", roomId);
                broadcastMsg.put("gameId", room.getGameId()); // 兼容旧代码
                broadcastMsg.put("playerGameIds", playerGameIds); // 新字段：每个玩家的 gameId
                broadcastMsg.put("playerRoles", playerRoles);
                broadcastMsg.put("totalPlayers", room.getPlayerCount());

                // ✅ 带超时和重试的广播
                int successCount = broadcastToRoomWithRetry(roomId, broadcastMsg, requestId, "START_GAME");
                
                Map<String, Object> logMap7 = new LinkedHashMap<>();
                logMap7.put("requestId", requestId);
                logMap7.put("roomId", roomId);
                logMap7.put("gameId", room.getGameId());
                logMap7.put("broadcastSuccessCount", successCount);
                logMap7.put("totalPlayers", room.getPlayerCount());
                log("INFO", "START_GAME_BROADCAST_COMPLETE", "游戏开始广播完成", logMap7);

                // ✅ 发送成功响应给房主
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("type", "START_GAME_RESULT");
                responseMap.put("success", true);
                responseMap.put("message", "游戏即将开始！已通知 " + successCount + "/" + room.getPlayerCount() + " 名玩家");
                responseMap.put("requestId", requestId);
                responseMap.put("roomId", roomId);
                responseMap.put("gameId", room.getGameId());
                sendMessage(session, responseMap);

            } finally {
                roomLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> logMap8 = new LinkedHashMap<>();
            logMap8.put("requestId", requestId);
            logMap8.put("roomId", roomId);
            logMap8.put("userId", userId);
            log("ERROR", "START_GAME_INTERRUPTED", "开始游戏被中断", logMap8);
            
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("type", "START_GAME_RESULT");
            responseMap.put("success", false);
            responseMap.put("message", "操作被中断，请重试");
            responseMap.put("requestId", requestId);
            sendMessage(session, responseMap);
        }
    }

    private void handleChat(WebSocketSession session, Map<String, Object> msg) {
        String roomId = (String) msg.getOrDefault("roomId", "UNKNOWN");
        String userId = String.valueOf(msg.getOrDefault("userId", "UNKNOWN"));
        String username = (String) msg.getOrDefault("username", "UNKNOWN");
        String content = (String) msg.getOrDefault("content", "");
        
        // 检查房间是否存在
        Room room = gameServer.getRoom(roomId);
        if (room == null) {
            log("WARN", "CHAT_MESSAGE", "房间不存在，无法发送消息", 
                Map.of("roomId", roomId, "userId", userId));
            return;
        }
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("roomId", roomId);
        logMap.put("userId", userId);
        logMap.put("username", username);
        logMap.put("contentLength", content != null ? content.length() : 0);
        logMap.put("roomStatus", room.getStatus());
        logMap.put("roomGameId", room.getGameId());
        logMap.put("roomPlayerCount", room.getPlayerCount());
        log("DEBUG", "CHAT_MESSAGE", "转发聊天消息", logMap);
        
        // 统计房间内在线连接数
        int onlineCount = 0;
        for (Map.Entry<WebSocketSession, String> entry : sessionRoomIds.entrySet()) {
            if (roomId.equals(entry.getValue()) && entry.getKey().isOpen()) {
                onlineCount++;
            }
        }
        log("DEBUG", "CHAT_ONLINE_COUNT", "房间在线连接数", 
            Map.of("roomId", roomId, "onlineCount", onlineCount, "totalPlayers", room.getPlayerCount()));
        
        broadcastToRoom(roomId, msg);
    }

    private int broadcastToRoomWithRetry(String roomId, Map<String, Object> msg, String requestId, String operationType) {
        Set<WebSocketSession> failedSessions = new HashSet<>();
        int successCount = 0;
        int totalCount = 0;

        // ✅ 第一轮广播
        successCount = broadcastToRoomWithTracking(roomId, msg, failedSessions);
        totalCount = failedSessions.size() + successCount;
        
        Map<String, Object> logMap1 = new LinkedHashMap<>();
        logMap1.put("requestId", requestId);
        logMap1.put("roomId", roomId);
        logMap1.put("successCount", successCount);
        logMap1.put("failedCount", failedSessions.size());
        logMap1.put("totalCount", totalCount);
        log("DEBUG", "BROADCAST_ROUND_1", "第一轮广播完成", logMap1);

        // ✅ 如果有失败，进行重试
        if (!failedSessions.isEmpty()) {
            for (int retry = 1; retry <= MAX_RETRY_COUNT; retry++) {
                try {
                    Thread.sleep(500 * retry); // 指数退避
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                Set<WebSocketSession> newFailed = new HashSet<>();
                int retrySuccess = broadcastToSpecificSessions(failedSessions, msg, newFailed);
                
                successCount += retrySuccess;
                failedSessions = newFailed;

                Map<String, Object> logMap2 = new LinkedHashMap<>();
                logMap2.put("requestId", requestId);
                logMap2.put("roomId", roomId);
                logMap2.put("retry", retry);
                logMap2.put("retrySuccess", retrySuccess);
                logMap2.put("remainingFailed", failedSessions.size());
                log("DEBUG", "BROADCAST_RETRY_" + retry, "重试广播完成", logMap2);

                if (failedSessions.isEmpty()) {
                    break;
                }
            }
        }

        // ✅ 记录最终结果
        if (!failedSessions.isEmpty()) {
            Map<String, Object> logMap3 = new LinkedHashMap<>();
            logMap3.put("requestId", requestId);
            logMap3.put("roomId", roomId);
            logMap3.put("operationType", operationType);
            logMap3.put("successCount", successCount);
            logMap3.put("failedCount", failedSessions.size());
            logMap3.put("totalCount", totalCount);
            log("WARN", "BROADCAST_PARTIAL_FAILURE", "部分客户端广播失败", logMap3);
        }

        return successCount;
    }

    private int broadcastToRoomWithTracking(String roomId, Map<String, Object> msg, Set<WebSocketSession> failedSessions) {
        int successCount = 0;
        try {
            String payload = objectMapper.writeValueAsString(msg);
            TextMessage textMessage = new TextMessage(payload);
            
            for (Map.Entry<WebSocketSession, String> entry : sessionRoomIds.entrySet()) {
                if (roomId.equals(entry.getValue())) {
                    WebSocketSession session = entry.getKey();
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                            successCount++;
                        } catch (IOException e) {
                            failedSessions.add(session);
                            String sessionId = "UNKNOWN";
                            try {
                                if (session != null) {
                                    sessionId = session.getId();
                                }
                            } catch (Exception ex) {
                                // Ignore
                            }
                            Map<String, Object> logMap = new LinkedHashMap<>();
                            logMap.put("sessionId", sessionId);
                            logMap.put("roomId", roomId);
                            logMap.put("error", e.getMessage());
                            log("WARN", "BROADCAST_SINGLE_FAILURE", "单个客户端广播失败", logMap);
                        }
                    } else {
                        failedSessions.add(session);
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("roomId", roomId);
            logMap.put("error", e.getMessage());
            log("ERROR", "BROADCAST_SERIALIZATION_ERROR", "广播消息序列化失败", logMap);
        }
        return successCount;
    }

    private int broadcastToSpecificSessions(Set<WebSocketSession> sessions, Map<String, Object> msg, Set<WebSocketSession> failedSessions) {
        int successCount = 0;
        try {
            String payload = objectMapper.writeValueAsString(msg);
            TextMessage textMessage = new TextMessage(payload);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                        successCount++;
                    } catch (IOException e) {
                        failedSessions.add(session);
                    }
                } else {
                    failedSessions.add(session);
                }
            }
        } catch (Exception e) {
            failedSessions.addAll(sessions);
        }
        return successCount;
    }

    private void broadcastToRoom(String roomId, Map<String, Object> msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            TextMessage textMessage = new TextMessage(payload);
            for (Map.Entry<WebSocketSession, String> entry : sessionRoomIds.entrySet()) {
                if (roomId.equals(entry.getValue())) {
                    WebSocketSession session = entry.getKey();
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                }
            }
        } catch (IOException e) {
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("roomId", roomId);
            logMap.put("error", e.getMessage());
            log("ERROR", "BROADCAST_ERROR", "广播消息错误", logMap);
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            session.sendMessage(new TextMessage(payload));
        } catch (IOException e) {
            String sessionId = "UNKNOWN";
            try {
                if (session != null) {
                    sessionId = session.getId();
                }
            } catch (Exception ex) {
                // Ignore
            }
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("sessionId", sessionId);
            logMap.put("error", e.getMessage());
            log("ERROR", "SEND_MESSAGE_ERROR", "发送消息错误", logMap);
        }
    }

    private void log(String level, String event, String message, Map<String, Object> details) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getTimestamp()).append("] ");
        sb.append("[").append(level).append("] ");
        sb.append("[").append(event).append("] ");
        sb.append(message);
        
        if (details != null && !details.isEmpty()) {
            sb.append(" | ");
            List<String> detailStrings = new ArrayList<>();
            details.forEach((k, v) -> {
                String valueStr = v instanceof List ? 
                    String.join(", ", ((List<?>) v).stream().map(Object::toString).toList()) : 
                    String.valueOf(v);
                detailStrings.add(k + "=" + valueStr);
            });
            sb.append(String.join("; ", detailStrings));
        }
        
        if ("ERROR".equals(level)) {
            System.err.println(sb);
        } else {
            System.out.println(sb);
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8) + "-" + System.currentTimeMillis();
    }

    /**
     * AI玩家自动选择角色
     * 当真人玩家选完角色后，AI玩家自动选择剩余的角色
     */
    private void autoSelectRolesForAI(String roomId) {
        Room room = gameServer.getRoom(roomId);
        if (room == null) {
            return;
        }

        // 获取所有已被选择的角色ID
        java.util.Set<String> selectedRoleIds = new java.util.HashSet<>();
        java.util.List<Player> humanPlayers = new java.util.ArrayList<>();
        java.util.List<Player> aiPlayers = new java.util.ArrayList<>();

        for (Player p : room.getPlayers()) {
            if (p.getRoleId() != null && !p.getRoleId().isEmpty()) {
                selectedRoleIds.add(p.getRoleId());
            }
            // 区分真人玩家和AI玩家
            if (p.getUserId() != null && p.getUserId().startsWith("ai_")) {
                aiPlayers.add(p);
            } else {
                humanPlayers.add(p);
            }
        }

        // 检查是否所有真人玩家都已选择角色
        boolean allHumansSelected = true;
        for (Player human : humanPlayers) {
            if (human.getRoleId() == null || human.getRoleId().isEmpty()) {
                allHumansSelected = false;
                break;
            }
        }

        // 如果还有真人玩家未选择角色，则AI玩家不自动选择
        if (!allHumansSelected) {
            log("DEBUG", "AUTO_SELECT_ROLE_SKIP", "等待真人玩家选择角色", 
                Map.of("roomId", roomId, "humanCount", humanPlayers.size(), 
                    "selectedHumanCount", selectedRoleIds.size(), "aiCount", aiPlayers.size()));
            return;
        }

        // AI玩家自动选择剩余角色
        java.util.List<String> availableRoleIds = new java.util.ArrayList<>();
        
        // 从GameService获取该剧本的所有角色
        try {
            Integer scriptId = Integer.parseInt(room.getScriptId());
            var roles = gameService.getRolesByScriptId(scriptId);
            
            for (var role : roles) {
                String roleIdStr = String.valueOf(role.getRoleId());
                if (!selectedRoleIds.contains(roleIdStr)) {
                    availableRoleIds.add(roleIdStr);
                }
            }
        } catch (Exception e) {
            log("ERROR", "AUTO_SELECT_ROLE_GET_ROLES_FAILED", "获取剧本角色失败", 
                Map.of("roomId", roomId, "scriptId", room.getScriptId(), "error", e.getMessage()));
            return;
        }

        // 为每个未选择角色的AI玩家分配一个角色
        int roleIndex = 0;
        for (Player ai : aiPlayers) {
            if (ai.getRoleId() == null || ai.getRoleId().isEmpty()) {
                if (roleIndex < availableRoleIds.size()) {
                    String roleId = availableRoleIds.get(roleIndex);
                    String roleName = getRoleNameById(room.getScriptId(), roleId);
                    
                    ai.setRoleId(roleId);
                    ai.setRoleName(roleName);
                    selectedRoleIds.add(roleId);
                    roleIndex++;

                    // 广播AI角色选择
                    Map<String, Object> aiSelectMsg = new LinkedHashMap<>();
                    aiSelectMsg.put("type", "ROLE_SELECT");
                    aiSelectMsg.put("requestId", generateRequestId());
                    aiSelectMsg.put("timestamp", getTimestamp());
                    aiSelectMsg.put("userId", ai.getUserId());
                    aiSelectMsg.put("roomId", roomId);
                    aiSelectMsg.put("roleId", roleId);
                    aiSelectMsg.put("roleName", roleName);

                    broadcastToRoom(roomId, aiSelectMsg);

                    log("INFO", "AUTO_SELECT_ROLE_SUCCESS", "AI玩家自动选择角色", 
                        Map.of("roomId", roomId, "aiUserId", ai.getUserId(), 
                            "aiUsername", ai.getUsername(), "roleId", roleId, "roleName", roleName));
                }
            }
        }
    }

    /**
     * 根据角色ID获取角色名称
     */
    private String getRoleNameById(String scriptIdStr, String roleId) {
        try {
            Integer scriptId = Integer.parseInt(scriptIdStr);
            var roles = gameService.getRolesByScriptId(scriptId);
            for (var role : roles) {
                if (String.valueOf(role.getRoleId()).equals(roleId)) {
                    return role.getName();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "未知角色";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = sessionUserIds.remove(session);
        String roomId = sessionRoomIds.remove(session);
        
        if (userId != null) {
            userSessions.remove(userId);
        }
        
        String sessionId = "UNKNOWN";
        String closeCode = "UNKNOWN";
        String closeReason = "UNKNOWN";
        try {
            if (session != null) {
                sessionId = session.getId();
                if (status != null) {
                    closeCode = String.valueOf(status.getCode());
                    if (status.getReason() != null) {
                        closeReason = status.getReason();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("sessionId", sessionId);
        logMap.put("userId", userId != null ? userId : "UNKNOWN");
        logMap.put("roomId", roomId != null ? roomId : "UNKNOWN");
        logMap.put("closeCode", closeCode);
        logMap.put("closeReason", closeReason);
        log("INFO", "CONNECTION_CLOSED", "连接关闭", logMap);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = sessionUserIds.get(session);
        String roomId = sessionRoomIds.get(session);
        
        String sessionId = "UNKNOWN";
        try {
            if (session != null) {
                sessionId = session.getId();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("sessionId", sessionId);
        logMap.put("userId", userId != null ? userId : "UNKNOWN");
        logMap.put("roomId", roomId != null ? roomId : "UNKNOWN");
        logMap.put("exceptionType", exception != null ? exception.getClass().getName() : "UNKNOWN");
        log("ERROR", "TRANSPORT_ERROR", "传输错误: " + (exception != null ? exception.getMessage() : "unknown error"), logMap);
    }
    
    // ✅ 内部类：记录角色选择请求
    private static class RoleSelectionRequest {
        String requestId;
        String userId;
        String roomId;
        String roleId;
        String roleName;
        long timestamp;
        int retryCount;
        
        RoleSelectionRequest(String requestId, String userId, String roomId, String roleId, String roleName) {
            this.requestId = requestId;
            this.userId = userId;
            this.roomId = roomId;
            this.roleId = roleId;
            this.roleName = roleName;
            this.timestamp = System.currentTimeMillis();
            this.retryCount = 0;
        }
    }
    
    // ✅ 内部类：记录游戏开始请求
    private static class GameStartRequest {
        String requestId;
        String userId;
        String roomId;
        long timestamp;
        boolean completed;
        
        GameStartRequest(String requestId, String userId, String roomId) {
            this.requestId = requestId;
            this.userId = userId;
            this.roomId = roomId;
            this.timestamp = System.currentTimeMillis();
            this.completed = false;
        }
    }
}
