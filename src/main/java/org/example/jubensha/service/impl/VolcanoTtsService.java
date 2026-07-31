package org.example.jubensha.service.impl;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VolcanoTtsService {

    @Value("${volcano.tts.app-id}")
    private String appId;

    @Value("${volcano.tts.access-token}")
    private String accessToken;

    @Value("${volcano.tts.voice-type}")
    private String voiceType;

    @Value("${file.upload-path}")
    private String uploadPath;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TTS_URL = "https://openspeech.bytedance.com/api/v1/tts";

    public String synthesize(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer;" + accessToken);

            Map<String, Object> app = new HashMap<>();
            app.put("appid", appId);
            app.put("token", accessToken);
            app.put("cluster", "volcano_tts");
            app.put("resource_id", voiceType);

            Map<String, Object> user = new HashMap<>();
            user.put("uid", "dm_host_01");

            Map<String, Object> audio = new HashMap<>();
            audio.put("voice_type", voiceType);
            audio.put("encoding", "mp3");
            audio.put("speed_ratio", 1.0);

            Map<String, Object> req = new HashMap<>();
            req.put("reqid", UUID.randomUUID().toString());
            req.put("text", text);
            req.put("text_type", "plain");
            req.put("operation", "query");

            Map<String, Object> body = new HashMap<>();
            body.put("app", app);
            body.put("user", user);
            body.put("audio", audio);
            body.put("request", req);
            body.put("resource_id", voiceType); // 资源ID通常就是音色ID

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(TTS_URL, entity, byte[].class);

            if (response.getBody() == null || response.getBody().length < 50) {
                System.err.println("[TTS] 火山引擎返回数据过短");
                return null;
            }

            // 解析 JSON 响应，提取 Base64 音频数据
            var json = JSON.parseObject(new String(response.getBody(), StandardCharsets.UTF_8));
            int code = json.getIntValue("code");
            if (code != 3000) {
                System.err.println("[TTS] 火山引擎错误: code=" + code + " " + json.getString("message"));
                return null;
            }

            String base64Data = json.getString("data");
            if (base64Data == null || base64Data.isEmpty()) {
                System.err.println("[TTS] 响应中没有音频数据");
                return null;
            }

            byte[] audioBytes = Base64.getDecoder().decode(base64Data);
            if (audioBytes.length < 100) {
                System.err.println("[TTS] 音频数据过短: " + audioBytes.length);
                return null;
            }

            File dir = new File(uploadPath + "voice/");
            if (!dir.exists()) dir.mkdirs();
            String fileName = "dm_" + System.currentTimeMillis() + ".mp3";
            try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
                fos.write(audioBytes);
            }

            String audioUrl = "/uploads/voice/" + fileName;
            System.out.println("[TTS] 语音生成成功: " + audioUrl + " (" + audioBytes.length + " bytes)");
            return audioUrl;

        } catch (Exception e) {
            System.err.println("[TTS] 火山引擎调用失败: " + e.getMessage());
            return null;
        }
    }
}
