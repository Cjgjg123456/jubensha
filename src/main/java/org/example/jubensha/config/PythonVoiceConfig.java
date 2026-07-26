package org.example.jubensha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Python 语音转文字服务配置
 * 
 * 当启用 Python 中间层时，Java 后端将调用 Python 服务进行语音识别
 * 
 * 配置项:
 *   voice.python.enabled - 是否启用 Python 中间层
 *   voice.python.host - Python 服务地址
 *   voice.python.port - Python 服务端口
 *   voice.python.timeout - 请求超时时间(秒)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "voice.python")
public class PythonVoiceConfig {

    /**
     * 是否启用 Python 中间层服务
     */
    private boolean enabled = false;

    /**
     * Python 服务主机地址
     */
    private String host = "localhost";

    /**
     * Python 服务端口
     */
    private int port = 5000;

    /**
     * 请求超时时间(秒)
     */
    private int timeout = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取完整的服务地址
     */
    public String getServiceUrl() {
        return String.format("http://%s:%d", host, port);
    }

    /**
     * 获取识别 API 地址
     */
    public String getRecognizeUrl() {
        return getServiceUrl() + "/api/recognize";
    }

    /**
     * 获取状态检查 API 地址
     */
    public String getStatusUrl() {
        return getServiceUrl() + "/api/status";
    }
}