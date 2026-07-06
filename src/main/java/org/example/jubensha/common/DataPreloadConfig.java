package org.example.jubensha.common;

import org.example.jubensha.entity.Script;
import org.example.jubensha.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataPreloadConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataPreloadConfig.class);

    @Autowired
    private GameService gameService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("========== 开始预加载游戏数据 ==========");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 预加载剧本列表
            List<Script> scripts = gameService.getScriptList();
            logger.info("预加载剧本列表完成，共 {} 个剧本", scripts.size());
            
            // 预加载每个剧本的角色和幕次
            for (Script script : scripts) {
                try {
                    gameService.getRolesByScriptId(script.getScriptId());
                    gameService.getActsByScriptId(script.getScriptId());
                } catch (Exception e) {
                    logger.warn("预加载剧本 {} 数据时出错: {}", script.getTitle(), e.getMessage());
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("========== 数据预加载完成，耗时 {}ms ==========", (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("数据预加载失败: {}", e.getMessage(), e);
        }
    }
}
