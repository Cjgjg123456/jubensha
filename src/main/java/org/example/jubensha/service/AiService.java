package org.example.jubensha.service;

import java.util.List;
import java.util.Map;

public interface AiService {
    // 新增：生成角色回复（单轮对话）
    String generateRoleReply(String prompt);

    // 新增：支持多轮对话上下文的方法
    String generateChatReply(List<Map<String, String>> messages);
}