package org.example.jubensha;

import org.example.jubensha.service.PythonVoiceServiceManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 线上剧本杀系统启动类
 */
@SpringBootApplication
@MapperScan("org.example.jubensha.mapper")
@EnableScheduling // 扫描Mapper接口
public class JubenshaApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JubenshaApplication.class, args);
        
        // 注册关闭钩子，确保Python服务随应用一起关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                PythonVoiceServiceManager voiceService = context.getBean(PythonVoiceServiceManager.class);
                voiceService.stopService();
            } catch (Exception e) {
                System.err.println("关闭Python服务时出错: " + e.getMessage());
            }
        }));
    }
}
