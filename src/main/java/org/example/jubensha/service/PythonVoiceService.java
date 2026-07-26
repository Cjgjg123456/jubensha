package org.example.jubensha.service;

import org.example.jubensha.config.PythonVoiceConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Python 语音转文字服务
 * 
 * 通过 HTTP 请求调用 Python 中间层服务进行语音识别
 * 支持 WAV、M4A、MP3 等多种音频格式
 */
@Service
public class PythonVoiceService {

    private final PythonVoiceConfig config;
    private final RestTemplate restTemplate;

    public PythonVoiceService(PythonVoiceConfig config) {
        this.config = config;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.max(1, config.getTimeout()) * 1000;
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * 检查 Python 服务状态
     */
    public boolean checkServiceStatus() {
        if (!config.isEnabled()) {
            System.out.println("⚠️ Python 语音服务未启用");
            return false;
        }

        try {
            String url = config.getStatusUrl();
            System.out.println("🔍 检查 Python 服务状态：" + url);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                Boolean modelLoaded = (Boolean) body.get("modelLoaded");
                Boolean serviceAvailable = (Boolean) body.get("serviceAvailable");
                
                if (Boolean.TRUE.equals(modelLoaded) && Boolean.TRUE.equals(serviceAvailable)) {
                    System.out.println("✅ Python 语音服务状态正常");
                    return true;
                } else {
                    System.out.println("⚠️ Python 服务状态异常：modelLoaded=" + modelLoaded + ", serviceAvailable=" + serviceAvailable);
                    return false;
                }
            } else {
                System.out.println("❌ Python 服务状态检查失败：" + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ 检查 Python 服务状态失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 调用 Python 服务进行语音识别
     * 
     * @param audioBytes 音频文件字节数组
     * @param fileName 文件名
     * @return 识别结果文本，如果失败返回 null
     */
    public String recognize(byte[] audioBytes, String fileName) {
        if (!config.isEnabled()) {
            System.out.println("⚠️ Python 语音服务未启用");
            return null;
        }

        if (audioBytes == null || audioBytes.length == 0) {
            System.err.println("❌ 音频数据为空");
            return null;
        }

        try {
            String url = config.getRecognizeUrl();
            System.out.println("🎤 调用 Python 语音识别服务：" + url + " (文件：" + fileName + ", 大小：" + audioBytes.length + " bytes)");

            // 构建请求体
            ByteArrayResource resource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                    new HttpEntity<>(body, headers);

            // 发送请求
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = response.getBody();
                Boolean success = (Boolean) result.get("success");
                
                if (Boolean.TRUE.equals(success)) {
                    String text = (String) result.get("text");
                    System.out.println("✅ Python 语音识别成功：" + text);
                    return text;
                } else {
                    String error = (String) result.get("error");
                    System.err.println("❌ Python 语音识别失败：" + error);
                    return null;
                }
            } else {
                System.err.println("❌ Python 语音识别服务返回错误：" + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ 调用 Python 语音识别服务失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断是否启用了 Python 服务
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }
}