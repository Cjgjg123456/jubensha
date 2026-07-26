package org.example.jubensha.config;

import jakarta.annotation.PostConstruct;
import org.example.jubensha.service.VoiceRecognitionService;
import org.example.jubensha.config.PythonVoiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Vosk 语音识别初始化配置
 * 在应用启动时自动加载语音识别模型
 */
@Configuration
public class VoiceRecognitionInitConfig {

    @Autowired
    private VoiceRecognitionService voiceRecognitionService;

    @Autowired
    private PythonVoiceConfig pythonVoiceConfig;

    /**
     * 应用启动时自动初始化 Vosk 模型
     */
    @PostConstruct
    public void initVoiceRecognition() {
        if (pythonVoiceConfig != null && pythonVoiceConfig.isEnabled()) {
            System.out.println("=== Python 语音服务已启用，跳过 Java Vosk 启动预加载 ===");
            return;
        }

        try {
            System.out.println("=== 开始初始化 Vosk 离线语音识别模型 ===");
            voiceRecognitionService.initModel();
            System.out.println("✅ Vosk 离线语音识别模型初始化成功！");
            System.out.println("🎤 离线语音转文字功能已就绪");
        } catch (Exception e) {
            System.err.println("⚠️  Vosk 模型初始化失败: " + e.getMessage());
            System.err.println("提示：语音识别功能可能无法使用，请检查模型文件是否存在");
            e.printStackTrace();
        }
    }
}