package org.example.jubensha.controller;

import org.example.jubensha.common.Result;
import org.example.jubensha.entity.Script;
import org.example.jubensha.mapper.GameMapper;
import org.example.jubensha.net.GameServer;
import org.example.jubensha.net.GameServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/online")
@CrossOrigin
public class OnlineController {

    private final GameServer gameServer;
    private final GameServerConfig config;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    public OnlineController(GameServerConfig config) {
        this.gameServer = GameServer.getInstance();
        this.config = config;
    }

    @GetMapping("/server/status")
    public Result<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", gameServer.isRunning());
        status.put("onlinePlayers", gameServer.getOnlinePlayerCount());
        status.put("rooms", gameServer.getRoomCount());
        status.put("port", config.getPort());
        status.put("publicIp", config.getPublicIp());
        status.put("publicPort", config.getPublicPort());
        return Result.success(status);
    }

    @GetMapping("/rooms")
    public Result<List<Map<String, Object>>> getRoomList() {
        List<Map<String, Object>> rooms = gameServer.getRoomList();
        return Result.success(rooms);
    }

    @GetMapping("/room/{roomId}")
    public Result<Map<String, Object>> getRoomInfo(@PathVariable String roomId) {
        Map<String, Object> room = gameServer.getRoomInfo(roomId);
        if (room != null) {
            return Result.success(room);
        } else {
            return Result.fail("房间不存在");
        }
    }

    @PostMapping("/server/start")
    public Result<String> startServer(@RequestParam(defaultValue = "8888") int port) {
        if (gameServer.isRunning()) {
            return Result.fail("服务器已启动");
        }
        boolean started = gameServer.start(port);
        if (started) {
            return Result.success("服务器启动成功，端口: " + port);
        } else {
            return Result.fail("服务器启动失败");
        }
    }

    @PostMapping("/server/stop")
    public Result<String> stopServer() {
        if (!gameServer.isRunning()) {
            return Result.fail("服务器未启动");
        }
        boolean stopped = gameServer.stop();
        if (stopped) {
            return Result.success("服务器已停止");
        } else {
            return Result.fail("服务器停止失败");
        }
    }

    @GetMapping("/config")
    public Result<GameServerConfig> getConfig() {
        return Result.success(config);
    }

    @PostMapping("/room/{roomId}/fill-with-ai")
    public Result<String> fillRoomWithAI(@PathVariable String roomId, 
                                         @RequestParam(defaultValue = "0") int target) {
        if (!gameServer.isRunning()) {
            return Result.fail("服务器未启动");
        }
        
        if (target > 0) {
            gameServer.fillRoomWithAI(roomId, target);
            return Result.success("已尝试将房间填充至 " + target + " 名玩家");
        } else {
            gameServer.fillRoomWithAI(roomId);
            return Result.success("已将房间填满AI玩家");
        }
    }

    @PostMapping("/room/create")
    public Result<Map<String, Object>> createRoom(@RequestParam String userId,
                                                   @RequestParam String username,
                                                   @RequestParam(required = false) String avatar,
                                                   @RequestParam String scriptId,
                                                   @RequestParam String roomName,
                                                   @RequestParam(defaultValue = "6") int playerCount) {
        // ✅ 根据剧本角色数量调整房间最大玩家数
        try {
            int scriptIdInt = Integer.parseInt(scriptId);
            int roleCount = gameMapper.getRolesByScriptId(scriptIdInt).size();
            if (roleCount > 0 && roleCount < playerCount) {
                playerCount = roleCount;
            }
        } catch (Exception e) {
            // 如果获取剧本信息失败，使用默认值
        }
        
        // 注意：REST API 不需要 Socket 服务器运行
        String roomId = gameServer.createRoomViaRest(userId, username, avatar, scriptId, roomName, playerCount);
        if (roomId != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("roomId", roomId);
            result.put("roomName", roomName);
            result.put("scriptId", scriptId);
            return Result.success(result);
        } else {
            return Result.fail("创建房间失败");
        }
    }

    @PostMapping("/room/join")
    public Result<String> joinRoom(@RequestParam String roomId,
                                    @RequestParam String userId,
                                    @RequestParam String username,
                                    @RequestParam(required = false) String avatar) {
        // 注意：REST API 不需要 Socket 服务器运行
        boolean success = gameServer.joinRoomViaRest(roomId, userId, username, avatar);
        if (success) {
            return Result.success("加入房间成功");
        } else {
            return Result.fail("加入房间失败，房间不存在、已满或已开始");
        }
    }

    @PostMapping("/room/match")
    public Result<Map<String, Object>> matchRecentRoom(@RequestParam String userId,
                                                        @RequestParam String username,
                                                        @RequestParam(required = false) String avatar,
                                                        @RequestParam(required = false, defaultValue = "all") String scriptType,
                                                        @RequestParam(required = false, defaultValue = "all") String playerCount,
                                                        @RequestParam(required = false, defaultValue = "all") String scriptDiff) {
        if (!hasText(userId) || !hasText(username) || "undefined".equals(userId) || "null".equals(userId)) {
            return Result.fail("登录状态已失效，请重新登录");
        }

        Integer requestedPlayerCount = parsePositiveInt(playerCount);
        Set<String> allowedScriptIds = buildAllowedScriptIds(scriptType, requestedPlayerCount, scriptDiff);
        if (allowedScriptIds != null && allowedScriptIds.isEmpty()) {
            return Result.fail("当前筛选条件下没有可匹配的剧本");
        }

        Map<String, Object> room = gameServer.matchRecentRoomViaRest(
                userId,
                username,
                avatar,
                allowedScriptIds,
                requestedPlayerCount,
                30_000L
        );

        if (room == null) {
            return Result.fail("暂无可加入的房间，请先创建房间");
        }

        enrichRoomWithScriptInfo(room);
        return Result.success(room);
    }

    @PostMapping("/room/leave")
    public Result<String> leaveRoom(@RequestParam String roomId,
                                    @RequestParam String userId) {
        gameServer.leaveRoomViaRest(roomId, userId);
        return Result.success("已离开房间");
    }

    @PostMapping("/room/{roomId}/chat")
    public Result<String> sendChat(@PathVariable String roomId,
                                   @RequestParam String userId,
                                   @RequestParam String username,
                                   @RequestParam(required = false) String avatar,
                                   @RequestParam String content,
                                   @RequestParam(required = false) String msgId) {
        // 注意：REST API 不需要 Socket 服务器运行
        boolean success = gameServer.sendChatViaRest(roomId, userId, username, avatar, content, msgId);
        if (success) {
            return Result.success("消息发送成功");
        } else {
            return Result.fail("发送消息失败");
        }
    }

    @GetMapping("/room/{roomId}/messages")
    public Result<List<Map<String, Object>>> getRoomMessages(@PathVariable String roomId) {
        List<Map<String, Object>> messages = gameServer.getRoomMessages(roomId);
        
        // 如果内存中没有消息，尝试从数据库加载（用于语音消息等）
        if (messages.isEmpty()) {
            try {
                Integer gameId = Integer.parseInt(roomId);
                List<Map<String, Object>> dbMessages = gameMapper.getChatRecords(gameId, null);
                if (dbMessages != null && !dbMessages.isEmpty()) {
                    messages = dbMessages.stream().map(m -> {
                        Map<String, Object> msg = new HashMap<>();
                        msg.put("username", m.get("sender_role_id") != null ? "角色" + m.get("sender_role_id") : "系统");
                        msg.put("content", m.get("content"));
                        msg.put("userId", String.valueOf(m.get("sender_role_id")));
                        return msg;
                    }).collect(Collectors.toList());
                }
            } catch (Exception e) {
                // 忽略解析错误，继续返回空列表
            }
        }
        
        return Result.success(messages);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Integer parsePositiveInt(String value) {
        if (!hasText(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Set<String> buildAllowedScriptIds(String scriptType, Integer playerCount, String scriptDiff) {
        boolean filterByType = hasText(scriptType) && !"all".equalsIgnoreCase(scriptType);
        boolean filterByCount = playerCount != null && playerCount > 0;
        boolean filterByDifficulty = hasText(scriptDiff) && !"all".equalsIgnoreCase(scriptDiff);

        if (!filterByType && !filterByCount && !filterByDifficulty) {
            return null;
        }

        Set<String> allowedScriptIds = new HashSet<>();
        List<Script> scripts = gameMapper.getScriptList();
        for (Script script : scripts) {
            if (script == null || script.getScriptId() == null) {
                continue;
            }
            if (filterByType && !matchesType(script, scriptType)) {
                continue;
            }
            if (filterByCount && (script.getPlayerCount() == null || !script.getPlayerCount().equals(playerCount))) {
                continue;
            }
            if (filterByDifficulty && !matchesDifficulty(script.getDifficulty(), scriptDiff)) {
                continue;
            }
            allowedScriptIds.add(String.valueOf(script.getScriptId()));
        }
        return allowedScriptIds;
    }

    private boolean matchesType(Script script, String scriptType) {
        String tags = script.getTags() == null ? "" : script.getTags();
        String title = script.getTitle() == null ? "" : script.getTitle();
        String intro = script.getIntro() == null ? "" : script.getIntro();
        return tags.contains(scriptType) || title.contains(scriptType) || intro.contains(scriptType);
    }

    private boolean matchesDifficulty(String difficulty, String selectedDiff) {
        Integer value = parseDifficultyValue(difficulty);
        if (value == null) {
            return true;
        }
        if ("1".equals(selectedDiff)) {
            return value <= 2;
        }
        if ("3".equals(selectedDiff)) {
            return value == 3 || value == 4;
        }
        if ("5".equals(selectedDiff)) {
            return value >= 5;
        }
        return true;
    }

    private Integer parseDifficultyValue(String difficulty) {
        if (!hasText(difficulty)) {
            return null;
        }
        String trimmed = difficulty.trim();
        try {
            return Integer.parseInt(trimmed.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            long stars = trimmed.chars().filter(ch -> ch == '★' || ch == '*').count();
            return stars > 0 ? (int) stars : null;
        }
    }

    private void enrichRoomWithScriptInfo(Map<String, Object> room) {
        Object scriptIdValue = room.get("scriptId");
        if (scriptIdValue == null) {
            return;
        }
        try {
            Script script = gameMapper.getScriptById(Integer.parseInt(String.valueOf(scriptIdValue)));
            if (script == null) {
                return;
            }
            room.put("scriptTitle", script.getTitle());
            room.put("scriptIntro", script.getIntro());
            room.put("scriptTags", script.getTags());
            room.put("scriptDifficulty", script.getDifficulty());
            room.put("scriptCoverUrl", script.getCoverUrl());
            room.put("scriptPlayerCount", script.getPlayerCount());
        } catch (Exception e) {
            // 房间本身仍可返回，剧本展示信息缺失不影响匹配结果
        }
    }
}