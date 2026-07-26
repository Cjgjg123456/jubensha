package org.example.jubensha.net;

import org.example.jubensha.net.entity.Player;
import org.example.jubensha.net.entity.Room;
import org.example.jubensha.net.msg.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameServer {
    private static GameServer instance;
    private ServerSocket serverSocket;
    private boolean running = false;
    private int port = 9999;

    private Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();
    private Map<String, Room> rooms = new ConcurrentHashMap<>();
    private Map<Socket, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private static final long ROOM_CLEANUP_INTERVAL_MINUTES = 5;
    private static final long IDLE_ROOM_TIMEOUT_MINUTES = 30;

    private GameServer() {}

    public static GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }

    public synchronized boolean start(int port) {
        if (running) {
            System.out.println("服务器已经启动");
            return false;
        }
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("服务器启动成功，监听端口: " + port);
            new AcceptThread().start();
            startRoomCleanupScheduler();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean stop() {
        if (!running) {
            return true;
        }
        running = false;
        stopRoomCleanupScheduler();
        try {
            serverSocket.close();
            for (ClientHandler handler : clientHandlers.values()) {
                handler.close();
            }
            clientHandlers.clear();
            onlinePlayers.clear();
            rooms.clear();
            System.out.println("服务器已停止");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private class AcceptThread extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("新客户端连接: " + clientSocket.getRemoteSocketAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clientHandlers.put(clientSocket, handler);
                    handler.start();
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private Player player;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            try {
                ois = new ObjectInputStream(clientSocket.getInputStream());
                oos = new ObjectOutputStream(clientSocket.getOutputStream());

                while (running) {
                    BaseMsg msg = (BaseMsg) ois.readObject();
                    msg.setClient(clientSocket);
                    handleMessage(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                handleDisconnect();
            }
        }

        private void handleMessage(BaseMsg msg) {
            System.out.println("收到消息: " + msg.getType());
            switch (msg.getType()) {
                case "LOGIN":
                    handleLogin((ClientLoginMsg) msg);
                    break;
                case "LOGOUT":
                    handleLogout((ClientLogoutMsg) msg);
                    break;
                case "CREATE_ROOM":
                    handleCreateRoom((ClientCreateRoomMsg) msg);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom((ClientJoinRoomMsg) msg);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom((ClientLeaveRoomMsg) msg);
                    break;
                case "CHAT":
                    handleChat((ClientChatMsg) msg);
                    break;
                case "READY":
                    handleReady((ClientReadyMsg) msg);
                    break;
                case "START_GAME":
                    handleStartGame((ClientStartGameMsg) msg);
                    break;
                case "SELECT_ROLE":
                    handleSelectRole((ClientSelectRoleMsg) msg);
                    break;
                case "SHARE_CLUE":
                    handleShareClue((ClientShareClueMsg) msg);
                    break;
                case "VOTE":
                    handleVote((ClientVoteMsg) msg);
                    break;
                case "ACCUSE":
                    handleAccuse((ClientAccuseMsg) msg);
                    break;
            }
        }

        private void handleLogin(ClientLoginMsg msg) {
            String userId = msg.getUserId();
            String username = msg.getUsername();
            
            if (onlinePlayers.containsKey(userId)) {
                sendMessage(new ServerLoginResultMsg(false, "用户已在线"));
                return;
            }

            Player player = new Player(userId, username, msg.getAvatar());
            player.setSocket(clientSocket);
            onlinePlayers.put(userId, player);
            this.player = player;

            sendMessage(new ServerLoginResultMsg(true, "登录成功", userId, username));
            System.out.println("用户登录: " + username);
        }

        private void handleLogout(ClientLogoutMsg msg) {
            if (player != null) {
                if (player.getRoomId() != null) {
                    leaveRoom(player.getUserId(), player.getRoomId());
                }
                onlinePlayers.remove(player.getUserId());
                System.out.println("用户退出: " + player.getUsername());
            }
        }

        private void handleCreateRoom(ClientCreateRoomMsg msg) {
            if (player == null) {
                sendMessage(new ServerCreateRoomResultMsg(false, "请先登录"));
                return;
            }

            String roomId = UUID.randomUUID().toString().substring(0, 8);
            Room room = new Room(roomId, msg.getRoomName(), msg.getScriptId(), msg.getUserId());
            
            // 根据剧本规定的人数设置房间最大玩家数
            int playerCount = msg.getPlayerCount();
            if (playerCount > 0) {
                room.setMaxPlayers(playerCount);
            }
            
            Player roomPlayer = new Player(player.getUserId(), player.getUsername(), player.getAvatar());
            roomPlayer.setRoomId(roomId);
            roomPlayer.setSocket(player.getSocket());  // ✅ 设置 Socket
            room.addPlayer(roomPlayer);
            
            rooms.put(roomId, room);
            player.setRoomId(roomId);

            sendMessage(new ServerCreateRoomResultMsg(true, "房间创建成功", roomId));
            System.out.println("房间创建: " + roomId + " by " + player.getUsername() + ", 最大人数: " + room.getMaxPlayers());
        }

        private void handleJoinRoom(ClientJoinRoomMsg msg) {
            if (player == null) {
                sendMessage(new ServerJoinRoomResultMsg(false, "请先登录"));
                return;
            }

            Room room = rooms.get(msg.getRoomId());
            if (room == null) {
                sendMessage(new ServerJoinRoomResultMsg(false, "房间不存在"));
                return;
            }

            if (room.isFull()) {
                sendMessage(new ServerJoinRoomResultMsg(false, "房间已满"));
                return;
            }

            System.out.println("[DEBUG] handleJoinRoom: 玩家 " + player.getUsername() + " 加入房间 " + msg.getRoomId() + ", Socket: " + player.getSocket());

            Player roomPlayer = new Player(player.getUserId(), player.getUsername(), player.getAvatar());
            roomPlayer.setRoomId(msg.getRoomId());
            roomPlayer.setSocket(player.getSocket());
            room.addPlayer(roomPlayer);
            player.setRoomId(msg.getRoomId());

            List<Map<String, Object>> playerList = room.getPlayers().stream()
                    .map(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("userId", p.getUserId());
                        map.put("username", p.getUsername());
                        map.put("avatar", p.getAvatar());
                        return map;
                    })
                    .collect(Collectors.toList());

            sendMessage(new ServerJoinRoomResultMsg(true, "加入成功", room.getRoomId(), playerList));

            ServerPlayerJoinMsg joinMsg = new ServerPlayerJoinMsg(
                    room.getRoomId(), player.getUserId(), player.getUsername(), player.getAvatar());
            broadcastToRoom(room.getRoomId(), joinMsg);

            System.out.println("用户加入房间: " + player.getUsername() + " -> " + room.getRoomId());
        }

        private void handleLeaveRoom(ClientLeaveRoomMsg msg) {
            leaveRoom(msg.getUserId(), msg.getRoomId());
        }

        private void leaveRoom(String userId, String roomId) {
            Room room = rooms.get(roomId);
            if (room != null) {
                room.removePlayer(userId);
                
                Player p = onlinePlayers.get(userId);
                if (p != null) {
                    p.setRoomId(null);
                }

                ServerPlayerLeaveMsg leaveMsg = new ServerPlayerLeaveMsg(roomId, userId);
                broadcastToRoom(roomId, leaveMsg);

                if (room.getPlayerCount() == 0) {
                    rooms.remove(roomId);
                    System.out.println("房间解散: " + roomId);
                }
            }
        }

        private void handleChat(ClientChatMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) {
                System.out.println("[DEBUG] handleChat: 房间不存在 - " + msg.getRoomId());
                return;
            }

            System.out.println("[DEBUG] handleChat: 收到消息 - 用户: " + player.getUsername() + ", 房间: " + msg.getRoomId() + ", 内容: " + msg.getContent());

            room.addMessage(player.getUserId(), player.getUsername(), player.getAvatar(), msg.getContent());

            ServerChatMsg chatMsg = new ServerChatMsg(
                    msg.getRoomId(),
                    player.getUserId(),
                    player.getUsername(),
                    player.getAvatar(),
                    msg.getContent()
            );
            broadcastToRoom(msg.getRoomId(), chatMsg);
        }

        private void handleReady(ClientReadyMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) {
                sendMessage(new ServerReadyResultMsg(false, "房间不存在"));
                return;
            }

            Player roomPlayer = room.getPlayer(msg.getUserId());
            if (roomPlayer != null) {
                roomPlayer.setReady(msg.isReady());
            }

            sendMessage(new ServerReadyResultMsg(true, "状态更新成功", msg.getUserId(), msg.isReady()));
            broadcastToRoom(msg.getRoomId(), new ServerReadyResultMsg(true, "", msg.getUserId(), msg.isReady()));
            System.out.println("玩家准备状态: " + player.getUsername() + " -> " + msg.isReady());
        }

        private void handleStartGame(ClientStartGameMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) {
                sendMessage(new ServerStartGameMsg(false, "房间不存在"));
                return;
            }

            // ✅ 只有房主可以开始游戏
            if (!room.getHostId().equals(msg.getUserId())) {
                sendMessage(new ServerStartGameMsg(false, "只有房主可以开始游戏"));
                return;
            }

            // ✅ 验证所有玩家都已选择角色
            boolean allPlayersSelectedRole = true;
            StringBuilder missingPlayers = new StringBuilder();
            for (Player p : room.getPlayers()) {
                if (p.getRoleId() == null || p.getRoleId().isEmpty()) {
                    allPlayersSelectedRole = false;
                    if (missingPlayers.length() > 0) {
                        missingPlayers.append(", ");
                    }
                    missingPlayers.append(p.getUsername());
                }
            }

            if (!allPlayersSelectedRole) {
                sendMessage(new ServerStartGameMsg(false, "请确保所有玩家都已选择角色！尚未选择角色的玩家: " + missingPlayers.toString()));
                return;
            }

            System.out.println("[DEBUG] handleStartGame: 玩家 " + player.getUsername() + " 开始游戏, 房间: " + msg.getRoomId());
            System.out.println("[DEBUG] handleStartGame: 房间玩家数: " + room.getPlayerCount());
            System.out.println("[DEBUG] handleStartGame: 已有消息数: " + room.getMessages().size());

            room.setStatus(Room.STATUS_PLAYING);
            
            // ✅ gameId 已在创建房间时生成，这里只需确认存在
            System.out.println("[DEBUG] handleStartGame: 使用 gameId=" + room.getGameId());
            
            // ✅ 获取已有的聊天记录
            java.util.List<java.util.Map<String, Object>> existingMessages = room.getMessages();
            
            // ✅ 发送包含聊天记录的游戏开始消息
            ServerStartGameMsg startMsg = new ServerStartGameMsg(true, "游戏开始!", msg.getRoomId(), existingMessages);
            broadcastToRoom(msg.getRoomId(), startMsg);

            System.out.println("游戏开始: " + room.getRoomId() + ", gameId=" + room.getGameId());
        }

        private void handleSelectRole(ClientSelectRoleMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) {
                sendMessage(new ServerRoleSelectMsg(false, "房间不存在"));
                return;
            }

            // ✅ 检查角色是否已被其他玩家选择
            String selectedRoleId = msg.getRoleId();
            for (Player p : room.getPlayers()) {
                if (!p.getUserId().equals(msg.getUserId()) && selectedRoleId.equals(p.getRoleId())) {
                    sendMessage(new ServerRoleSelectMsg(false, "该角色已被其他玩家选择，请选择其他角色"));
                    System.out.println("[DEBUG] handleSelectRole: 角色 " + msg.getRoleName() + " 已被选择，拒绝玩家 " + player.getUsername() + " 的选择");
                    return;
                }
            }

            Player roomPlayer = room.getPlayer(msg.getUserId());
            if (roomPlayer != null) {
                roomPlayer.setRoleId(msg.getRoleId());
                roomPlayer.setRoleName(msg.getRoleName());
            }

            System.out.println("[DEBUG] handleSelectRole: 玩家 " + player.getUsername() + " 选择角色 " + msg.getRoleName());

            ServerRoleSelectMsg selectMsg = new ServerRoleSelectMsg(
                    true, "角色选择成功", msg.getRoomId(), msg.getUserId(), msg.getRoleId(), msg.getRoleName());
            sendMessage(selectMsg);
            broadcastToRoom(msg.getRoomId(), selectMsg);
            System.out.println("角色选择: " + player.getUsername() + " -> " + msg.getRoleName());
        }

        private void handleShareClue(ClientShareClueMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) return;

            ServerClueShareMsg shareMsg = new ServerClueShareMsg(
                    msg.getRoomId(),
                    player.getUserId(),
                    player.getUsername(),
                    player.getAvatar(),
                    msg.getClueId(),
                    msg.getClueName(),
                    msg.getClueDescription()
            );
            broadcastToRoom(msg.getRoomId(), shareMsg);
            System.out.println("线索分享: " + player.getUsername() + " -> " + msg.getClueName());
        }

        private void handleVote(ClientVoteMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) return;

            ServerVoteMsg voteMsg = new ServerVoteMsg(
                    msg.getRoomId(),
                    player.getUserId(),
                    player.getUsername(),
                    msg.getTargetUserId(),
                    msg.getTargetUsername()
            );
            broadcastToRoom(msg.getRoomId(), voteMsg);
            System.out.println("投票: " + player.getUsername() + " -> " + msg.getTargetUsername());
        }

        private void handleAccuse(ClientAccuseMsg msg) {
            if (player == null) return;

            Room room = rooms.get(msg.getRoomId());
            if (room == null) return;

            ServerAccuseMsg accuseMsg = new ServerAccuseMsg(
                    msg.getRoomId(),
                    player.getUserId(),
                    player.getUsername(),
                    msg.getTargetUserId(),
                    msg.getTargetUsername(),
                    msg.getEvidence(),
                    msg.getReasoning()
            );
            broadcastToRoom(msg.getRoomId(), accuseMsg);
            System.out.println("指控: " + player.getUsername() + " -> " + msg.getTargetUsername());
        }

        private void broadcastToRoom(String roomId, BaseMsg msg) {
            Room room = rooms.get(roomId);
            if (room == null) {
                System.out.println("[DEBUG] broadcastToRoom: 房间不存在 - " + roomId);
                return;
            }

            System.out.println("[DEBUG] broadcastToRoom: 房间 " + roomId + " 共有 " + room.getPlayerCount() + " 名玩家");

            for (Player p : room.getPlayers()) {
                Socket socket = p.getSocket();
                System.out.println("[DEBUG] broadcastToRoom: 玩家 " + p.getUsername() + ", Socket: " + socket + ", 已关闭: " + (socket != null && socket.isClosed()));

                if (socket != null && !socket.isClosed()) {
                    ClientHandler handler = clientHandlers.get(socket);
                    System.out.println("[DEBUG] broadcastToRoom: 玩家 " + p.getUsername() + " 的 ClientHandler: " + handler);

                    if (handler != null) {
                        handler.sendMessage(msg);
                        System.out.println("[DEBUG] broadcastToRoom: 消息已发送给 " + p.getUsername());
                    } else {
                        System.out.println("[DEBUG] broadcastToRoom: 未找到玩家 " + p.getUsername() + " 的 ClientHandler");
                    }
                }
            }
        }

        public void sendMessage(BaseMsg msg) {
            try {
                System.out.println("[DEBUG] sendMessage: 发送消息类型 - " + msg.getType() + " 给玩家: " + (player != null ? player.getUsername() : "未知"));
                oos.writeObject(msg);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleDisconnect() {
            if (player != null) {
                if (player.getRoomId() != null) {
                    leaveRoom(player.getUserId(), player.getRoomId());
                }
                onlinePlayers.remove(player.getUserId());
                System.out.println("用户断开连接: " + player.getUsername());
            }
            clientHandlers.remove(clientSocket);
            close();
        }

        public void close() {
            try {
                if (ois != null) ois.close();
                if (oos != null) oos.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageTo(Socket socket, BaseMsg msg) {
        ClientHandler handler = clientHandlers.get(socket);
        if (handler != null) {
            handler.sendMessage(msg);  // ✅ 使用已有的 ObjectOutputStream
        }
    }

    public List<Map<String, Object>> getRoomList() {
        return rooms.values().stream()
                .filter(r -> Room.STATUS_IDLE.equals(r.getStatus()))
                .filter(r -> r.getPlayerCount() > 0)
                .filter(Room::hasRealPlayers)
                .sorted((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()))
                .limit(1)
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomId", r.getRoomId());
                    map.put("roomName", r.getRoomName());
                    map.put("scriptId", r.getScriptId());
                    map.put("hostId", r.getHostId());
                    map.put("status", r.getStatus());
                    map.put("playerCount", r.getPlayerCount());
                    map.put("maxPlayers", r.getMaxPlayers());
                    map.put("createTime", r.getCreateTime());
                    map.put("gameId", r.getGameId());

                    List<Map<String, Object>> playerList = r.getPlayers().stream()
                            .map(p -> {
                                Map<String, Object> playerMap = new HashMap<>();
                                playerMap.put("userId", p.getUserId());
                                playerMap.put("username", p.getUsername());
                                playerMap.put("avatar", p.getAvatar());
                                playerMap.put("ready", p.isReady());
                                playerMap.put("roleId", p.getRoleId());
                                playerMap.put("roleName", p.getRoleName());
                                return playerMap;
                            })
                            .collect(Collectors.toList());
                    map.put("players", playerList);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Map<String, Object> getRoomInfo(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", room.getRoomId());
        map.put("roomName", room.getRoomName());
        map.put("scriptId", room.getScriptId());
        map.put("hostId", room.getHostId());
        map.put("status", room.getStatus());
        map.put("playerCount", room.getPlayerCount());
        map.put("maxPlayers", room.getMaxPlayers());
        map.put("createTime", room.getCreateTime());
        map.put("gameId", room.getGameId());

        List<Map<String, Object>> playerList = room.getPlayers().stream()
                .map(p -> {
                    Map<String, Object> playerMap = new HashMap<>();
                    playerMap.put("userId", p.getUserId());
                    playerMap.put("username", p.getUsername());
                    playerMap.put("avatar", p.getAvatar());
                    playerMap.put("ready", p.isReady());
                    playerMap.put("roleId", p.getRoleId());
                    playerMap.put("roleName", p.getRoleName());
                    return playerMap;
                })
                .collect(Collectors.toList());
        map.put("players", playerList);

        return map;
    }

    public synchronized String createRoomViaRest(String userId, String username, String avatar, String scriptId, String roomName, int playerCount) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        Room room = new Room(roomId, roomName, scriptId, userId);
        room.setMaxPlayers(playerCount);
        
        Player roomPlayer = new Player(userId, username, avatar != null ? avatar : "");
        roomPlayer.setRoomId(roomId);
        room.addPlayer(roomPlayer);
        
        rooms.put(roomId, room);
        
        System.out.println("[REST API] 房间创建成功: " + roomId + " by " + username + ", 最大人数: " + playerCount);
        return roomId;
    }

    public synchronized boolean joinRoomViaRest(String roomId, String userId, String username, String avatar) {
        Room room = rooms.get(roomId);
        if (room == null) {
            System.out.println("[REST API] 加入房间失败，房间不存在: " + roomId);
            return false;
        }

        if (!Room.STATUS_IDLE.equals(room.getStatus())) {
            System.out.println("[REST API] 加入房间失败，房间不在等待状态: " + roomId);
            return false;
        }

        if (room.getPlayer(userId) != null) {
            System.out.println("[REST API] " + username + " 已在房间中: " + roomId);
            return true;
        }

        Player roomPlayer = new Player(userId, username, avatar != null ? avatar : "");
        roomPlayer.setRoomId(roomId);
        if (!room.addPlayer(roomPlayer)) {
            System.out.println("[REST API] 加入房间失败，房间已满或用户重复: " + roomId);
            return false;
        }

        System.out.println("[REST API] " + username + " 加入房间: " + roomId);
        return true;
    }

    public synchronized Map<String, Object> matchRecentRoomViaRest(String userId,
                                                                    String username,
                                                                    String avatar,
                                                                    Set<String> allowedScriptIds,
                                                                    Integer playerCount,
                                                                    long withinMillis) {
        long now = System.currentTimeMillis();

        Room existingRoom = findUserRoom(userId);
        if (existingRoom != null) {
            if (isMatchCandidate(existingRoom, userId, allowedScriptIds, playerCount, true)) {
                System.out.println("[REST API] 用户已锁定匹配房间，直接返回: " + existingRoom.getRoomId());
                return getRoomInfo(existingRoom.getRoomId());
            }
            System.out.println("[REST API] 匹配失败，用户已在其他房间中: " + userId);
            return null;
        }

        List<Room> candidates = rooms.values().stream()
                .filter(room -> isMatchCandidate(room, userId, allowedScriptIds, playerCount, false))
                .filter(Room::hasRealPlayers)
                .sorted((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()))
                .limit(1)
                .collect(Collectors.toList());

        for (Room room : candidates) {
            if (!isMatchCandidate(room, userId, allowedScriptIds, playerCount, false)) {
                continue;
            }
            Player roomPlayer = new Player(userId, username, avatar != null ? avatar : "");
            roomPlayer.setRoomId(room.getRoomId());
            if (room.addPlayer(roomPlayer)) {
                System.out.println("[REST API] 匹配成功: " + username + " -> " + room.getRoomId());
                return getRoomInfo(room.getRoomId());
            }
        }

        System.out.println("[REST API] 未找到可加入房间: " + username);
        return null;
    }

    public synchronized boolean leaveRoomViaRest(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return true;
        }
        boolean removed = room.removePlayer(userId);
        if (removed) {
            System.out.println("[REST API] 用户离开房间: " + userId + " -> " + roomId);
        }
        if (room.getPlayerCount() == 0) {
            rooms.remove(roomId);
            System.out.println("[REST API] 房间无人，已解散: " + roomId);
        }
        return true;
    }

    private Room findUserRoom(String userId) {
        if (userId == null) return null;
        return rooms.values().stream()
                .filter(room -> room.getPlayer(userId) != null)
                .findFirst()
                .orElse(null);
    }

    private boolean isMatchCandidate(Room room,
                                     String userId,
                                     Set<String> allowedScriptIds,
                                     Integer playerCount,
                                     boolean allowExistingUser) {
        if (room == null) return false;
        if (!Room.STATUS_IDLE.equals(room.getStatus())) return false;
        boolean userAlreadyInRoom = room.getPlayer(userId) != null;
        if (room.getHostId() != null && room.getHostId().equals(userId) && !allowExistingUser) return false;
        if (!allowExistingUser && userAlreadyInRoom) return false;
        if (!userAlreadyInRoom && room.isFull()) return false;
        if (allowedScriptIds != null && !allowedScriptIds.isEmpty() && !allowedScriptIds.contains(room.getScriptId())) return false;
        if (playerCount != null && playerCount > 0 && room.getMaxPlayers() != playerCount) return false;
        return true;
    }

    public synchronized boolean sendChatViaRest(String roomId, String userId, String username, String avatar, String content) {
        return sendChatViaRest(roomId, userId, username, avatar, content, null);
    }

    public synchronized boolean sendChatViaRest(String roomId, String userId, String username, String avatar, String content, String msgId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            System.out.println("[REST API] 发送消息失败，房间不存在: " + roomId);
            return false;
        }
        
        // ✅ 添加消息到房间列表
        room.addMessage(userId, username, avatar != null ? avatar : "", content, msgId);

        // ✅ 创建聊天消息
        ServerChatMsg chatMsg = new ServerChatMsg(roomId, userId, username, avatar, content);
        chatMsg.setMsgId(msgId);
        
        // ✅ 通过 Socket 广播给在线玩家
        System.out.println("[REST API] 开始广播消息到房间 " + roomId + " 的 " + room.getPlayerCount() + " 名玩家");
        broadcastToRoom(roomId, chatMsg);
        
        System.out.println("[REST API] " + username + " 在房间 " + roomId + " 发送消息: " + content);
        return true;
    }

    public List<Map<String, Object>> getRoomMessages(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return new java.util.ArrayList<>();
        }
        return room.getMessages();
    }

    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public void broadcastToRoom(String roomId, BaseMsg msg) {
        Room room = rooms.get(roomId);
        if (room == null) return;

        for (Player p : room.getPlayers()) {
            Socket socket = p.getSocket();
            if (socket != null && !socket.isClosed()) {
                sendMessageTo(socket, msg);
            }
        }
    }

    public void fillRoomWithAI(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            AIPlayerManager.getInstance().fillRoomWithAI(room);
        }
    }

    public void fillRoomWithAI(String roomId, int targetPlayerCount) {
        Room room = rooms.get(roomId);
        if (room != null) {
            AIPlayerManager.getInstance().fillRoomWithAI(room, targetPlayerCount);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastToAll(BaseMsg msg) {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(msg);
        }
        System.out.println("[DEBUG] 广播消息给所有 " + clientHandlers.size() + " 个客户端");
    }

    /**
     * 获取在线用户列表
     */
    public List<Map<String, Object>> getOnlineUsers() {
        return onlinePlayers.values().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", p.getUserId());
                    map.put("username", p.getUsername());
                    map.put("avatar", p.getAvatar());
                    map.put("roomId", p.getRoomId());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据用户名查找玩家
     */
    public Player findPlayerByUsername(String username) {
        return onlinePlayers.values().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查用户是否已在线
     */
    public boolean isUserOnline(String userId) {
        return onlinePlayers.containsKey(userId);
    }

    /**
     * 绑定用户到客户端连接
     */
    public void bindPlayerToSocket(Player player, Socket socket) {
        ClientHandler handler = clientHandlers.get(socket);
        if (handler != null) {
            handler.setPlayer(player);
        }
    }

    /**
     * 移除客户端连接
     */
    public void removeClient(Socket socket) {
        clientHandlers.remove(socket);
    }

    private void startRoomCleanupScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            cleanupIdleRooms();
        }, 0, ROOM_CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println("房间定时清理任务已启动，每 " + ROOM_CLEANUP_INTERVAL_MINUTES + " 分钟检查一次");
    }

    private void stopRoomCleanupScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            scheduler = null;
            System.out.println("房间定时清理任务已停止");
        }
    }

    private synchronized void cleanupIdleRooms() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = IDLE_ROOM_TIMEOUT_MINUTES * 60 * 1000;

        List<String> roomsToRemove = rooms.entrySet().stream()
                .filter(entry -> {
                    Room room = entry.getValue();
                    long age = currentTime - room.getCreateTime();
                    // 空房间立即清除
                    if (room.getPlayerCount() == 0) {
                        return true;
                    }
                    // IDLE/FINISHED 超时清除
                    boolean isIdleOrFinished = Room.STATUS_IDLE.equals(room.getStatus())
                            || Room.STATUS_FINISHED.equals(room.getStatus());
                    return isIdleOrFinished && age > timeoutMillis;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!roomsToRemove.isEmpty()) {
            System.out.println("开始清理过期房间，共 " + roomsToRemove.size() + " 个房间");
            for (String roomId : roomsToRemove) {
                Room room = rooms.remove(roomId);
                System.out.println("清理房间: " + roomId + " (" + room.getRoomName() + "), 创建时间: " + room.getCreateTime());
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = GameServer.getInstance();
        server.start(9999);
    }
}