package org.example.jubensha.net;

import org.example.jubensha.net.msg.*;
import org.example.jubensha.net.util.ConfigManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameClient {
    private static GameClient instance;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean connected = false;
    private MessageHandler messageHandler;

    private String serverIp;
    private int serverPort;

    private GameClient() {
        ConfigManager config = ConfigManager.getInstance();
        this.serverIp = config.getProperty("server.ip");
        this.serverPort = config.getIntProperty("server.port");
        System.out.println("游戏客户端初始化 - 服务器地址: " + serverIp + ":" + serverPort);
    }

    public static GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }
        return instance;
    }

    public void setServerConfig(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    public synchronized boolean connect() {
        if (connected) {
            return true;
        }
        try {
            socket = new Socket(serverIp, serverPort);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            connected = true;
            new ReceiveThread().start();
            System.out.println("连接服务器成功: " + serverIp + ":" + serverPort);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean disconnect() {
        if (!connected) {
            return true;
        }
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
            connected = false;
            System.out.println("已断开连接");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendLogin(String userId, String username, String avatar) {
        sendMessage(new ClientLoginMsg(userId, username, avatar));
    }

    public void sendLogout(String userId) {
        sendMessage(new ClientLogoutMsg(userId));
    }

    public void sendCreateRoom(String userId, String scriptId, String roomName) {
        sendMessage(new ClientCreateRoomMsg(userId, scriptId, roomName));
    }

    public void sendJoinRoom(String userId, String roomId) {
        sendMessage(new ClientJoinRoomMsg(userId, roomId));
    }

    public void sendLeaveRoom(String userId, String roomId) {
        sendMessage(new ClientLeaveRoomMsg(userId, roomId));
    }

    public void sendChat(String userId, String roomId, String content) {
        sendMessage(new ClientChatMsg(userId, roomId, content));
    }

    private void sendMessage(BaseMsg msg) {
        if (!connected) {
            System.out.println("未连接服务器");
            return;
        }
        try {
            oos.writeObject(msg);
            oos.flush();
            System.out.println("发送消息: " + msg.getType());
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            while (connected) {
                try {
                    BaseMsg msg = (BaseMsg) ois.readObject();
                    if (messageHandler != null) {
                        messageHandler.onMessage(msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    connected = false;
                    if (messageHandler != null) {
                        messageHandler.onDisconnect();
                    }
                }
            }
        }
    }

    public interface MessageHandler {
        void onMessage(BaseMsg msg);
        void onDisconnect();
    }
}