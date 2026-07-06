package org.example.jubensha.controller;

import org.example.jubensha.common.Result;
import org.example.jubensha.mapper.GameMapper;
import org.example.jubensha.net.GameServer;
import org.example.jubensha.net.GameServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            return Result.fail("加入房间失败，房间不存在或已满");
        }
    }

    @PostMapping("/room/{roomId}/chat")
    public Result<String> sendChat(@PathVariable String roomId,
                                   @RequestParam String userId,
                                   @RequestParam String username,
                                   @RequestParam(required = false) String avatar,
                                   @RequestParam String content) {
        // 注意：REST API 不需要 Socket 服务器运行
        boolean success = gameServer.sendChatViaRest(roomId, userId, username, avatar, content);
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
}