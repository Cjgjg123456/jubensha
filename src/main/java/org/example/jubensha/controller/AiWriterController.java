package org.example.jubensha.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import org.example.jubensha.common.Result;
import org.example.jubensha.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 剧本创作 AI 助手
 * 读取创作者已写的剧本上下文（标题/简介/公共剧情/角色/线索等），
 * 支持续写、润色、逻辑建议、自定义指令、多角色视角生成、线索卡建议。
 */
@RestController
@RequestMapping("/api/ai")
public class AiWriterController {

    @Autowired
    private AiService aiService;

    /**
     * 创作助手主入口
     * body: {
     *   task: "continue" | "polish" | "suggest" | "custom" | "rolesView" | "clues"
     *   context: { title, intro, acts[], publicContent[], roleIndex, actIndex,
     *              roles[], currentContent, additional[] }
     *   instruction: "自由补充指令（可选）"
     * }
     */
    @PostMapping("/writer")
    public Result<Map<String, Object>> writer(@RequestBody Map<String, Object> payload) {
        try {
            String task = String.valueOf(payload.getOrDefault("task", "custom"));
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) payload.getOrDefault("context", new HashMap<>());
            String instruction = payload.get("instruction") != null ? String.valueOf(payload.get("instruction")) : "";

            // 构建上下文文本
            String contextText = buildContextText(task, context);
            // 按任务构造 system + user 消息
            List<Map<String, String>> messages = buildMessages(task, contextText, instruction);

            // ⚠️ deepseek-v4-pro 是推理模型：推理(reasoning_content)与正文共享 max_tokens，
            //    2500 会被推理耗尽导致 content 为空。必须给足额度：
            //    续写/润色等正文任务 8000；rolesView/clues 推理与输出量更大用 12000
            int maxTokens = ("rolesView".equals(task) || "clues".equals(task)) ? 12000 : 8000;
            String reply = aiService.generateChatReply(messages, maxTokens);

            Map<String, Object> data = new HashMap<>();
            data.put("text", reply);
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("AI 创作服务异常：" + e.getMessage());
        }
    }

    /** 组装剧本上下文纯文本 */
    private String buildContextText(String task, Map<String, Object> ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append("【剧本名称】").append(nullToStr(ctx.get("title"))).append("\n");
        sb.append("【剧本简介】").append(nullToStr(ctx.get("intro"))).append("\n\n");

        // 幕次列表
        List<String> acts = toStringList(ctx.get("acts"));
        List<String> publics = toStringList(ctx.get("publicContent"));

        // 当前编辑位置信息（由前端传入）
        int actIndex = ctx.get("actIndex") instanceof Number ? ((Number) ctx.get("actIndex")).intValue() : -1;
        int roleIndex = ctx.get("roleIndex") instanceof Number ? ((Number) ctx.get("roleIndex")).intValue() : -1;

        // 全部角色及其人设
        List<Object> rolesRaw = toList(ctx.get("roles"));
        if (!rolesRaw.isEmpty()) {
            sb.append("【全部角色与人设】\n");
            for (int i = 0; i < rolesRaw.size(); i++) {
                Map<?, ?> r = (Map<?, ?>) rolesRaw.get(i);
                sb.append("  ").append(i + 1).append(". ").append(nullToStr(r.get("name")))
                  .append(" —— ").append(nullToStr(r.get("intro"))).append("\n");
            }
            sb.append("\n");
        }

        // 每幕公共剧情（写全局建议时全给；写单幕时重点给当前幕 + 前情）
        sb.append("【每幕公共剧情】\n");
        for (int i = 0; i < acts.size(); i++) {
            String actName = i < acts.size() ? String.valueOf(acts.get(i)) : ("第" + (i + 1) + "幕");
            String pub = i < publics.size() ? String.valueOf(publics.get(i)) : "";
            boolean isCurrent = (i == actIndex);
            sb.append("  — 第").append(i + 1).append("幕 ").append(actName)
              .append(isCurrent ? " ★当前编辑幕★" : "").append("：")
              .append(truncate(pub, isCurrent ? 4000 : 600)).append("\n");
        }
        sb.append("\n");

        // 当前角色各幕内容（续写/润色用）
        if (roleIndex >= 0 && roleIndex < rolesRaw.size()) {
            Map<?, ?> cur = (Map<?, ?>) rolesRaw.get(roleIndex);
            sb.append("【当前目标角色：").append(nullToStr(cur.get("name"))).append("】\n");
            List<String> contents = toStringList(cur.get("actsContent"));
            for (int i = 0; i < contents.size(); i++) {
                boolean isCurrent = (i == actIndex);
                sb.append("  · 第").append(i + 1).append("幕").append(isCurrent ? " ★当前编辑★" : "")
                  .append("：").append(truncate(String.valueOf(contents.get(i)), isCurrent ? 4000 : 500)).append("\n");
            }
            sb.append("\n");
        }

        // 当前正在编辑的原文（续写的起点）
        String currentContent = nullToStr(ctx.get("currentContent"));
        if (!currentContent.isEmpty()) {
            sb.append("【当前待处理文本（续写/润色目标）】\n").append(truncate(currentContent, 6000)).append("\n\n");
        }

        // 用户额外参考（如已知真相、想补的伏笔）
        List<String> additional = toStringList(ctx.get("additional"));
        if (!additional.isEmpty()) {
            sb.append("【创作者补充设定】\n");
            for (String a : additional) sb.append("  · ").append(a).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    /** 按任务拼装 system/user */
    private List<Map<String, String>> buildMessages(String task, String contextText, String instruction) {
        String sys = "你是资深剧本杀（谋杀之谜）剧本创作专家，精通本格与变格推理、人物塑造、伏笔与节奏设计。"
                + "请基于创作者提供的剧本内容，写出**可直接粘贴进剧本编辑器**的高质量中文正文，"
                + "语言生动有画面感，人物口吻与身份一致，不要输出解释性前言，不要加【】式标记（除非另有说明），不要使用Markdown标题。";
        String user;

        switch (task) {
            case "continue":
                user = "请**接着上文继续创作**，延续文风与叙事视角，让剧情自然推进。\n\n" + contextText
                        + (instruction.isEmpty() ? "" : "\n\n创作者额外要求：" + instruction);
                break;
            case "polish":
                user = "请对【当前待处理文本】**润色重写**：保留原有情节与人设，优化语言、节奏与细节描写，"
                        + "使文字更精炼有张力。若发现逻辑瑕疵可微调并自然弥合。\n\n" + contextText
                        + (instruction.isEmpty() ? "" : "\n\n创作者额外要求：" + instruction);
                break;
            case "suggest":
                user = "请以专业编辑视角**审查这份剧本**，输出：\n1）亮点\n2）逻辑漏洞/时间线矛盾\n3）伏笔埋设建议\n"
                        + "4）每个角色信息分配是否均衡（谁信息量过大/过小）\n5）节奏与反转建议\n6）可执行的改进清单（分条）\n\n"
                        + contextText + (instruction.isEmpty() ? "" : "\n\n创作者额外要求：" + instruction);
                break;
            case "rolesView":
                user = "请**为本幕的每位角色生成各自的专属剧情视角**（他们各自看到/知道/隐瞒的部分）。"
                        + "输出时每个角色一段，段首用【角色名】标记，方便创作者识别后分别粘贴。"
                        + "视角之间要互相咬合但信息不等，可包含时间线片段、私人秘密与行动动机，且不提前暴露真凶结论。\n\n"
                        + contextText + (instruction.isEmpty() ? "" : "\n\n创作者额外要求：" + instruction);
                break;
            case "clues":
                user = "请根据当前幕的剧情，**设计 4~6 条高价值线索卡**。每条用如下格式输出，便于创作者录入：\n"
                        + "【线索名】xxx\n【类型】公开线索 / 私人线索\n【内容】客观描述（不直接点名结论，留给玩家推理）\n"
                        + "【解锁幕】第几幕\n【作用】这条线索暗示了什么（给创作者的备注，不会给玩家看）\n\n"
                        + contextText + (instruction.isEmpty() ? "" : "\n\n创作者额外要求：" + instruction);
                break;
            default: // custom
                user = contextText + "\n\n请按以下指令创作/修改：" + (instruction.isEmpty() ? "（请基于剧本背景自由发挥一段精彩剧情）" : instruction);
                break;
        }
        if (user == null) user = contextText;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", sys));
        messages.add(Map.of("role", "user", "content", user));
        return messages;
    }

    private String nullToStr(Object o) { return o == null ? "" : String.valueOf(o); }

    @SuppressWarnings("unchecked")
    private List<Object> toList(Object o) {
        if (o instanceof List) return (List<Object>) o;
        return new ArrayList<>();
    }

    private List<String> toStringList(Object o) {
        List<String> out = new ArrayList<>();
        for (Object item : toList(o)) out.add(nullToStr(item));
        return out;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) + "……(略)" : s;
    }
}
