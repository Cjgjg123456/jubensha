package org.example.jubensha.net;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GameServerRunner implements CommandLineRunner {

    private final GameServerConfig config;

    @Autowired
    public GameServerRunner(GameServerConfig config) {
        this.config = config;
    }

    @Override
    public void run(String... args) throws Exception {
        GameServer server = GameServer.getInstance();
        boolean started = server.start(config.getPort());
        if (started) {
            System.out.println("=== 剧本杀联机服务器启动成功 ===");
            System.out.println("本地端口: " + config.getPort());
            System.out.println("公网访问: " + config.getPublicIp() + ":" + config.getPublicPort());
            System.out.println("===============================");
        } else {
            System.out.println("联机服务器启动失败");
        }
    }
}