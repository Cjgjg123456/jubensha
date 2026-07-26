package org.example.jubensha.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.example.jubensha.service.AiService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {
    // 新增：DeepSeek API 密钥
    private static final String API_KEY = "sk-ace24dfb825148d3a71344db171744fd";
    // 新增：DeepSeek API 地址（使用自定义端点）
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    // 新增：自定义端点 ID（替换为您在 DeepSeek 控制台创建的端点 ID）
    private static final String ENDPOINT_ID = "deepseek-v4-pro";
    // 新增：RestTemplate 用于发送 HTTP 请求
    private final RestTemplate restTemplate = new RestTemplate();
    // 新增：生成角色回复（单轮对话）
    @Override
    public String generateRoleReply(String prompt) {
        // 新增：将单轮对话转换为多轮对话格式
        List<Map<String, String>> messages = new ArrayList<>();
        // 新增：添加系统角色的初始提示
        Map<String, String> userMessage = new HashMap<>();
        // 新增：添加用户角色的提示
        userMessage.put("role", "user");
        // 新增：添加用户角色的提示内容
        userMessage.put("content", prompt);
        // 新增：将用户消息添加到多轮对话列表中
        messages.add(userMessage);
        // 新增：调用处理多轮聊天的方法
        return generateChatReply(messages); // 复用新方法
    }

    // 新增：处理真正的多轮聊天
    @Override
    public String generateChatReply(List<Map<String, String>> messages) {
        // 新增：构建请求体
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ENDPOINT_ID);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1500);

            // 将拼装好的历史消息（包含 system, user, assistant）直接传给大模型
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("AI服务返回空响应");
            }

            JSONObject jsonObject = JSON.parseObject(response.getBody());
            
            if (!jsonObject.containsKey("choices") || jsonObject.getJSONArray("choices").isEmpty()) {
                throw new RuntimeException("AI服务返回格式异常");
            }

            String reply = jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return reply != null ? reply.trim() : "【系统波动】AI陷入了沉思，请再说一次。";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("AI服务调用失败: " + e.getMessage());
            return "【系统波动】AI陷入了沉思，请再说一次。";
        }
    }
}