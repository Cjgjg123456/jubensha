package org.example.jubensha.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Python语音服务管理器
 * 负责自动检测和启动Python语音识别服务
 */
@Service
public class PythonVoiceServiceManager {
    
    private static final Logger log = LoggerFactory.getLogger(PythonVoiceServiceManager.class);
    
    // Python服务配置
    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/api/health";
    private static final String PYTHON_SCRIPT_PATH = "python_voice_server_enhanced.py";
    private static final int STARTUP_TIMEOUT = 30000; // 30秒超时
    
    private Process pythonProcess = null;
    private boolean serviceStarted = false;
    
    /**
     * Spring应用准备就绪后自动执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("🔍 开始检查Python语音服务状态...");
        
        // 检查服务是否已在运行
        if (isServiceRunning()) {
            log.info("✅ Python语音服务已在运行");
            serviceStarted = true;
            return;
        }
        
        // 服务未运行，尝试自动启动
        log.warn("⚠️ Python语音服务未运行，尝试自动启动...");
        startPythonService();
    }
    
    /**
     * 检查Python服务是否在运行
     */
    private boolean isServiceRunning() {
        try {
            URL url = new URL(PYTHON_SERVICE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            return responseCode == 200;
        } catch (Exception e) {
            log.debug("Python服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 启动Python语音服务
     */
    private void startPythonService() {
        try {
            // 检查Python脚本是否存在
            File scriptFile = new File(PYTHON_SCRIPT_PATH);
            if (!scriptFile.exists()) {
                log.error("❌ Python脚本文件不存在: {}", PYTHON_SCRIPT_PATH);
                log.error("请将 {} 放在项目根目录下", PYTHON_SCRIPT_PATH);
                return;
            }
            
            log.info("📄 找到Python脚本: {}", scriptFile.getAbsolutePath());
            
            // 构建启动命令
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows系统
                processBuilder = new ProcessBuilder("python", PYTHON_SCRIPT_PATH);
            } else {
                // Linux/Mac系统
                processBuilder = new ProcessBuilder("python3", PYTHON_SCRIPT_PATH);
            }
            
            // 设置工作目录为项目根目录
            processBuilder.directory(new File(System.getProperty("user.dir")));
            
            // 重定向错误流到标准输出
            processBuilder.redirectErrorStream(true);
            
            log.info("🚀 正在启动Python语音服务...");
            pythonProcess = processBuilder.start();
            
            // 异步读取Python服务的输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Python] {}", line);
                    }
                } catch (Exception e) {
                    log.error("读取Python服务输出失败", e);
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();
            
            // 等待服务启动
            log.info("⏳ 等待Python服务启动（最多{}秒）...", STARTUP_TIMEOUT / 1000);
            long startTime = System.currentTimeMillis();
            boolean started = false;
            
            while (System.currentTimeMillis() - startTime < STARTUP_TIMEOUT) {
                Thread.sleep(1000);
                
                if (isServiceRunning()) {
                    started = true;
                    break;
                }
                
                // 检查进程是否还在运行
                if (pythonProcess != null && !pythonProcess.isAlive()) {
                    log.error("❌ Python进程已退出");
                    break;
                }
            }
            
            if (started) {
                serviceStarted = true;
                log.info("✅ Python语音服务启动成功！");
                log.info("📍 服务地址: http://localhost:5000");
                log.info("📡 API端点: POST /api/recognize");
            } else {
                log.error("❌ Python语音服务启动超时或失败");
                log.error("请手动运行: python {}", PYTHON_SCRIPT_PATH);
                
                // 尝试终止进程
                if (pythonProcess != null && pythonProcess.isAlive()) {
                    pythonProcess.destroy();
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 启动Python语音服务失败", e);
            log.error("请确保:");
            log.error("1. Python已安装并添加到PATH");
            log.error("2. Flask已安装: pip install flask");
            log.error("3. Vosk模型文件存在: vosk-model-small-cn-0.22");
            log.error("4. libvosk.dll文件存在");
        }
    }
    
    /**
     * 获取服务状态
     */
    public boolean isServiceStarted() {
        return serviceStarted;
    }
    
    /**
     * 停止Python服务
     */
    public void stopService() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            log.info("🛑 正在停止Python语音服务...");
            pythonProcess.destroy();
            
            try {
                pythonProcess.waitFor();
                log.info("✅ Python语音服务已停止");
            } catch (InterruptedException e) {
                log.error("停止Python服务时被中断", e);
                Thread.currentThread().interrupt();
            }
        }
        serviceStarted = false;
    }
}
