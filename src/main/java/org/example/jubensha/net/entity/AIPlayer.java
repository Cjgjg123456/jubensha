package org.example.jubensha.net.entity;

import java.util.Random;

public class AIPlayer extends Player {
    private static final String[] AI_NAMES = {
        "侦探小明", "神秘人", "推理达人", "逻辑专家", "心理分析师",
        "福尔摩斯", "柯南", "狄仁杰", "包拯", "华生"
    };
    
    private static final String[] AI_AVATARS = {
        "/uploads/default_role.jpg", "/uploads/default_role.jpg", "/uploads/default_role.jpg",
        "/uploads/default_role.jpg", "/uploads/default_role.jpg"
    };
    
    private static final String[] CHAT_MESSAGES = {
        "这个线索很有意思...",
        "我觉得凶手可能是...",
        "让我分析一下这个证据",
        "大家怎么看这个问题？",
        "我有一个想法...",
        "这个房间有点可疑",
        "让我们梳理一下时间线",
        "我需要更多的信息",
        "这个角色的动机是什么？",
        "证据指向了谁呢？"
    };
    
    private static final String[] QUESTIONS = {
        "你觉得这个线索重要吗？",
        "为什么你会这么认为？",
        "能解释一下你的推理吗？",
        "你有什么证据支持这个观点？",
        "其他人有什么看法？"
    };
    
    private final Random random = new Random();
    private String roleName;
    
    public AIPlayer(String roomId) {
        super();
        int index = random.nextInt(AI_NAMES.length);
        setUserId("ai_" + System.currentTimeMillis() + "_" + index);
        setUsername(AI_NAMES[index]);
        setAvatar(AI_AVATARS[random.nextInt(AI_AVATARS.length)]);
        setRoomId(roomId);
    }
    
    public AIPlayer(String roomId, String roleName) {
        this(roomId);
        this.roleName = roleName;
    }
    
    public String generateChatMessage() {
        double rand = random.nextDouble();
        if (rand < 0.7) {
            return CHAT_MESSAGES[random.nextInt(CHAT_MESSAGES.length)];
        } else {
            return QUESTIONS[random.nextInt(QUESTIONS.length)];
        }
    }
    
    public boolean shouldChat() {
        return random.nextDouble() < 0.3;
    }
    
    public long getChatInterval() {
        return 5000 + random.nextLong(10000);
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}