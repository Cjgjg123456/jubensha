package org.example.jubensha.net;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.server")
public class GameServerConfig {
    private String ip = "localhost";
    private int port = 8888;
    private String publicIp = "11.tcp.cpolar.top";
    private int publicPort = 10980;
    private int maxPlayersPerRoom = 6;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    public int getMaxPlayersPerRoom() {
        return maxPlayersPerRoom;
    }

    public void setMaxPlayersPerRoom(int maxPlayersPerRoom) {
        this.maxPlayersPerRoom = maxPlayersPerRoom;
    }
}