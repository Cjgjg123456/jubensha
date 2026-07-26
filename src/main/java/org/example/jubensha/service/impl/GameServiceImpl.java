package org.example.jubensha.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.jubensha.common.Result;
import org.example.jubensha.entity.*;
import org.example.jubensha.mapper.GameMapper;
import org.example.jubensha.service.AiService;
import org.example.jubensha.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    @Autowired private GameMapper gameMapper;
    @Autowired private AiService aiService;

    private String getRecentChatContext(Integer gameId) {
        List<Map<String, Object>> recentChats = gameMapper.getRecentGlobalChatRecords(gameId);
        if (recentChats == null || recentChats.isEmpty()) return "暂无历史讨论记录。";

        GameProgress progress = gameMapper.getGameProgress(gameId);
        List<Role> roles = gameMapper.getRolesByScriptId(progress.getScriptId());
        Map<Integer, String> roleMap = new HashMap<>();
        for(Role r : roles) roleMap.put(r.getRoleId(), r.getName());
        roleMap.put(0, "🎤 DM 主持人");

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> r : recentChats) {
            Integer roleId = (Integer) r.get("sender_role_id");
            String content = (String) r.get("content");
            String roleName = roleMap.getOrDefault(roleId, "玩家");
            sb.append("【").append(roleName).append("】: ").append(content).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Cacheable(value = "scriptList", key = "'all'")
    public List<Script> getScriptList() { return gameMapper.getScriptList(); }

    @Override
    public List<Script> getScriptsByUserId(Long userId) { return gameMapper.getScriptsByUserId(userId); }

    @Override
    @Cacheable(value = "roles", key = "#scriptId")
    public List<Role> getRolesByScriptId(Integer scriptId) { return gameMapper.getRolesByScriptId(scriptId); }

    @Override
    @Cacheable(value = "acts", key = "#scriptId")
    public List<Act> getActsByScriptId(Integer scriptId) { return gameMapper.getActsByScriptId(scriptId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "scriptList", key = "'all'")
    public Integer createNewScript(Map<String, Object> scriptData) {
        Script script = mapToScript(scriptData);
        gameMapper.insertScript(script);
        saveComponents(script.getScriptId(), scriptData);
        return script.getScriptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"scriptList", "roles", "acts"}, allEntries = true)
    public Integer updateExistingScript(Map<String, Object> scriptData) {
        Integer scriptId = Integer.valueOf(scriptData.get("scriptId").toString());
        String username = (String) scriptData.getOrDefault("username", scriptData.get("author"));
        Long userId = username != null ? gameMapper.getUserIdByUsername(username) : null;
        
        // 权限检查：只有作者才能编辑剧本
        Script existingScript = gameMapper.getScriptById(scriptId);
        if (existingScript == null) {
            throw new RuntimeException("剧本不存在");
        }
        if (userId == null || !userId.equals(existingScript.getUserId())) {
            throw new RuntimeException("只有剧本作者才能编辑此剧本");
        }
        
        gameMapper.deleteActsByScriptId(scriptId);
        gameMapper.deleteRolesByScriptId(scriptId);
        gameMapper.deleteRoleActContentsByScriptId(scriptId);
        gameMapper.deleteCluesByScriptId(scriptId);
        gameMapper.deleteEndingsByScriptId(scriptId);
        gameMapper.deleteArchitecturesByScriptId(scriptId);

        Script script = mapToScript(scriptData);
        script.setScriptId(scriptId);
        gameMapper.updateScript(script);
        saveComponents(scriptId, scriptData);
        return scriptId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"scriptList", "roles", "acts"}, allEntries = true)
    public void deleteScriptById(Integer scriptId, String username) {
        Long userId = username != null ? gameMapper.getUserIdByUsername(username) : null;
        
        // 权限检查：只有作者才能删除剧本
        Script existingScript = gameMapper.getScriptById(scriptId);
        if (existingScript == null) {
            throw new RuntimeException("剧本不存在");
        }
        if (userId == null || !userId.equals(existingScript.getUserId())) {
            throw new RuntimeException("只有剧本作者才能删除此剧本");
        }
        
        gameMapper.deleteActsByScriptId(scriptId);
        gameMapper.deleteRolesByScriptId(scriptId);
        gameMapper.deleteRoleActContentsByScriptId(scriptId);
        gameMapper.deleteCluesByScriptId(scriptId);
        gameMapper.deleteEndingsByScriptId(scriptId);
        gameMapper.deleteArchitecturesByScriptId(scriptId);
        gameMapper.deleteScriptById(scriptId);
    }

    private Script mapToScript(Map<String, Object> data) {
        Script s = new Script();
        
        s.setTitle((String) data.getOrDefault("title", data.get("scriptName")));
        s.setDifficulty(data.get("difficulty") != null ? data.get("difficulty").toString() : "1");
        s.setIntro((String) data.getOrDefault("intro", data.get("introduction")));
        s.setCoverUrl((String) data.get("coverUrl"));
        s.setBgmUrl((String) data.get("bgmUrl"));

        Integer playerCount = null;
        if (data.containsKey("playerCount")) {
            playerCount = (Integer) data.get("playerCount");
        } else if (data.containsKey("config")) {
            String config = (String) data.get("config");
            playerCount = config != null ? Integer.parseInt(config.replaceAll("[^0-9]", "")) : 1;
        } else if (data.containsKey("total_players")) {
            playerCount = (Integer) data.get("total_players");
        }
        s.setPlayerCount(playerCount != null ? playerCount : 4);
        s.setTags("原创制作");
        s.setTruthContent((String) data.get("truthContent"));

        String username = (String) data.getOrDefault("username", data.get("author"));
        s.setUserId(username != null ? gameMapper.getUserIdByUsername(username) : 1L);

        if (data.containsKey("seriesId") && data.get("seriesId") != null) {
            String sIdStr = data.get("seriesId").toString();
            if (!sIdStr.trim().isEmpty() && !sIdStr.equals("0")) {
                Integer sId = Integer.valueOf(sIdStr);
                s.setSeriesId(sId);
                Integer maxOrder = gameMapper.getMaxSeriesOrder(sId);
                s.setSeriesOrder(maxOrder != null ? maxOrder + 1 : 1);
            }
        }
        return s;
    }

    private void saveComponents(Integer scriptId, Map<String, Object> data) {
        List<String> actNames = (List<String>) data.get("actNames");
        List<String> actContents = (List<String>) data.get("actContents");

        List<Integer> actIds = new ArrayList<>();
        if (actNames != null) {
            for (int i = 0; i < actNames.size(); i++) {
                Act act = new Act();
                act.setScriptId(scriptId);
                act.setActName(actNames.get(i));
                act.setSort(i + 1);
                if (actContents != null && i < actContents.size()) {
                    act.setPublicContent(actContents.get(i));
                }
                gameMapper.insertAct(act);
                actIds.add(act.getActId());
            }
        }

        List<Map<String, Object>> characters = (List<Map<String, Object>>) data.get("characters");
        List<Integer> roleIds = new ArrayList<>();
        if (characters != null) {
            for (int i = 0; i < characters.size(); i++) {
                Map<String, Object> c = characters.get(i);
                Role role = new Role();
                role.setScriptId(scriptId);
                role.setName((String) c.get("name"));
                role.setBackground((String) c.get("intro"));
                role.setIsAi(i == 0 ? 0 : 1);

                String customAvatar = (String) c.get("avatar");
                if (customAvatar != null && !customAvatar.trim().isEmpty()) {
                    role.setAvatar(customAvatar);
                } else {
                    role.setAvatar("/uploads/16.png");
                }

                gameMapper.insertRole(role);
                roleIds.add(role.getRoleId());

                List<String> contents = (List<String>) c.get("actsContent");
                if (contents != null && !actIds.isEmpty()) {
                    for (int j = 0; j < contents.size() && j < actIds.size(); j++) {
                        RoleActContent rac = new RoleActContent();
                        rac.setScriptId(scriptId);
                        rac.setActId(actIds.get(j));
                        rac.setRoleId(role.getRoleId());
                        rac.setContent(contents.get(j));
                        gameMapper.insertRoleActContent(rac);
                    }
                }
            }
        }

        List<Map<String, Object>> clues = (List<Map<String, Object>>) data.get("clues");
        if (clues != null) {
            for (Map<String, Object> c : clues) {
                ScriptClue sc = new ScriptClue();
                sc.setScriptId(Long.valueOf(scriptId));
                sc.setClueName((String) c.get("name"));
                sc.setClueDesc((String) c.get("desc"));
                sc.setIsPublic(Integer.valueOf(c.get("isPublic").toString()));

                // 🟢 修复 Index -1 越界漏洞：加上 >= 0 的安全边界检查
                int aidx = Integer.valueOf(c.get("actIndex").toString());
                if (aidx >= 0 && aidx < actIds.size()) {
                    sc.setUnlockChapterId(Long.valueOf(actIds.get(aidx)));
                }

                if (sc.getIsPublic() == 0) {
                    int ridx = Integer.valueOf(c.get("roleIndex").toString());
                    // 🟢 修复 Index -1 越界漏洞：加上 >= 0 的安全边界检查
                    if (ridx >= 0 && ridx < roleIds.size()) {
                        sc.setRoleId(roleIds.get(ridx));
                    }
                }

                if (c.containsKey("isHidden")) sc.setIsHidden(Integer.valueOf(c.get("isHidden").toString()));
                if (c.containsKey("unlockCondition")) sc.setUnlockCondition((String) c.get("unlockCondition"));
                if (c.containsKey("sceneImageUrl")) sc.setSceneImageUrl((String) c.get("sceneImageUrl"));

                gameMapper.insertScriptClue(sc);
            }
        }

        List<Map<String, Object>> endings = (List<Map<String, Object>>) data.get("endings");
        if (endings != null) {
            for (Map<String, Object> e : endings) {
                ScriptEnding ending = new ScriptEnding();
                ending.setScriptId(scriptId);
                ending.setEndingTitle((String) e.get("title"));
                ending.setEndingContent((String) e.get("content"));

                int vIndex = Integer.parseInt(e.get("votedRoleIndex").toString());
                if (vIndex >= 0 && vIndex < roleIds.size()) {
                    ending.setVotedRoleId(roleIds.get(vIndex));
                } else if (vIndex == -1) {
                    ending.setVotedRoleId(-1);
                } else if (vIndex == -2) {
                    ending.setVotedRoleId(0);
                } else {
                    ending.setVotedRoleId(-1);
                }
                gameMapper.insertScriptEnding(ending);
            }
        }

        List<Map<String, Object>> architectures = (List<Map<String, Object>>) data.get("architectures");
        if (architectures != null) {
            for (Map<String, Object> a : architectures) {
                ScriptArchitecture arch = new ScriptArchitecture();
                arch.setScriptId(scriptId);
                arch.setArchName((String) a.get("archName"));
                arch.setImageUrl((String) a.get("imageUrl"));
                arch.setArchDesc((String) a.get("archDesc"));
                gameMapper.insertArchitecture(arch);
            }
        }
    }

    @Override
    public Map<String, Object> getScriptFullDetail(Integer id) {
        Map<String, Object> res = new HashMap<>();
        res.put("script", gameMapper.getScriptById(id));
        res.put("acts", gameMapper.getActsByScriptId(id));
        res.put("roles", gameMapper.getRolesByScriptId(id));
        res.put("clues", gameMapper.getAllCluesByScriptId(id));
        res.put("contents", gameMapper.getAllContentsByScriptId(id));
        res.put("endings", gameMapper.getEndingsByScriptId(id));
        res.put("architectures", gameMapper.getArchitecturesByScriptId(id));
        return res;
    }

    @Override
    public GameProgress startGame(Long userId, Integer scriptId, Integer roleId) {
        System.out.println("========================================");
        System.out.println("  开始游戏请求");
        System.out.println("========================================");
        System.out.println("  userId: " + userId);
        System.out.println("  scriptId: " + scriptId);
        System.out.println("  roleId: " + roleId);
        
        // ✅ 检查该角色是否已被其他玩家选择（排除当前用户自己的旧进度）
        List<GameProgress> existingGames = gameMapper.getGameProgressByScript(scriptId);
        System.out.println("  现有游戏进度数: " + (existingGames != null ? existingGames.size() : 0));
        
        for (GameProgress existing : existingGames) {
            // 如果是其他用户占用了该角色，则报错
            if (!existing.getUserId().equals(userId) && 
                existing.getUserRoleId() != null && 
                existing.getUserRoleId().equals(roleId)) {
                String errorMsg = "该角色已被其他玩家选择，请选择其他角色！";
                System.err.println("❌ " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        
        // ✅ 清理当前用户在该剧本中的旧游戏进度（AI模式支持重新开局）
        for (GameProgress existing : existingGames) {
            if (existing.getUserId().equals(userId)) {
                System.out.println("  清理旧进度: gameId=" + existing.getGameId());
                gameMapper.deleteGameProgress(existing.getGameId());
            }
        }
        
        List<Act> acts = gameMapper.getActsByScriptId(scriptId);
        List<Role> roles = gameMapper.getRolesByScriptId(scriptId);
        
        System.out.println("  幕次数: " + (acts != null ? acts.size() : 0));
        System.out.println("  角色数: " + (roles != null ? roles.size() : 0));
        
        if (acts == null || acts.isEmpty()) {
            String errorMsg = "该剧本没有配置幕次，无法开局！";
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        if (roles == null || roles.isEmpty()) {
            String errorMsg = "该剧本没有配置角色，无法开局！";
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // ✅ 验证选择的角色是否存在于该剧本中
        boolean roleExists = roles.stream().anyMatch(r -> r.getRoleId().equals(roleId));
        System.out.println("  角色是否存在: " + roleExists);
        
        if (!roleExists) {
            // 打印所有可用角色ID以便调试
            System.out.println("  可用角色ID:");
            for (Role r : roles) {
                System.out.println("    - " + r.getRoleId() + ": " + r.getName());
            }
            String errorMsg = "选择的角色不存在于该剧本中！角色ID: " + roleId + ", 剧本ID: " + scriptId;
            System.err.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        GameProgress p = new GameProgress();
        p.setUserId(userId); p.setScriptId(scriptId); p.setUserRoleId(roleId);
        p.setCurrentActId(acts.get(0).getActId()); p.setPhase("BACKGROUND");
        gameMapper.insertGameProgress(p);
        distributeClues(p);
        
        System.out.println("✅ 游戏创建成功，gameId: " + p.getGameId());
        System.out.println("========================================");
        
        return p;
    }
    
    @Override
    public GameProgress startGameForOnline(Long userId, Integer scriptId, Integer roleId) {
        // ✅ 联机模式：不检查角色冲突（因为房间已经管理了角色分配）
        List<GameProgress> existingGames = gameMapper.getGameProgressByScript(scriptId);
        
        // ✅ 清理当前用户在该剧本中的旧游戏进度
        for (GameProgress existing : existingGames) {
            if (existing.getUserId().equals(userId)) {
                gameMapper.deleteGameProgress(existing.getGameId());
            }
        }
        
        List<Act> acts = gameMapper.getActsByScriptId(scriptId);
        List<Role> roles = gameMapper.getRolesByScriptId(scriptId);
        if (acts.isEmpty() || roles.isEmpty()) throw new RuntimeException("该剧本配置不完整，无法开局！");

        // ✅ 验证选择的角色是否存在于该剧本中
        boolean roleExists = roles.stream().anyMatch(r -> r.getRoleId().equals(roleId));
        if (!roleExists) {
            throw new RuntimeException("选择的角色不存在于该剧本中！");
        }

        GameProgress p = new GameProgress();
        p.setUserId(userId); p.setScriptId(scriptId); p.setUserRoleId(roleId);
        p.setCurrentActId(acts.get(0).getActId()); p.setPhase("BACKGROUND");
        gameMapper.insertGameProgress(p);
        distributeClues(p);
        return p;
    }

    @Override
    public Map<String, Object> getActContentWithCheck(Integer gameId, Integer targetActId) {
        if (gameId == null) {
            throw new RuntimeException("游戏ID不能为空");
        }
        
        GameProgress p = gameMapper.getGameProgress(gameId);
        if (p == null) {
            throw new RuntimeException("游戏进度不存在，请重新创建房间开始游戏");
        }
        
        Integer scriptId = p.getScriptId();
        Integer roleId = p.getUserRoleId();
        Integer actId = (targetActId != null) ? targetActId : p.getCurrentActId();
        
        if (scriptId == null || roleId == null || actId == null) {
            throw new RuntimeException("游戏数据不完整，请重新创建房间");
        }
        
        Act ta = gameMapper.getActById(actId);
        RoleActContent rc = gameMapper.getRoleActContent(scriptId, actId, roleId);

        Map<String, Object> r = new HashMap<>();
        r.put("actId", actId);
        r.put("actName", ta != null ? ta.getActName() : "第一幕");
        r.put("publicContent", ta != null ? ta.getPublicContent() : "");
        r.put("privateContent", rc != null ? rc.getContent() : "");
        r.put("scriptId", scriptId);
        r.put("roleId", roleId);
        return r;
    }

    private void distributeClues(GameProgress progress) {
        List<ScriptClue> clues = gameMapper.getCluesByAct(progress.getScriptId(), progress.getCurrentActId());
        List<Act> acts = gameMapper.getActsByScriptId(progress.getScriptId());
        if (acts == null || acts.isEmpty() || clues.isEmpty()) return;

        boolean isFirstAct = progress.getCurrentActId().equals(acts.get(0).getActId());
        if (isFirstAct) {
            for (ScriptClue clue : clues) {
                if (clue.getIsHidden() == null || clue.getIsHidden() == 0) {
                    gameMapper.insertUnlockedClue(progress.getGameId(), clue.getClueId());
                }
            }
        } else {
            List<ScriptClue> publicClues = new ArrayList<>();
            for (ScriptClue clue : clues) {
                if ((clue.getIsPublic() != null && clue.getIsPublic() == 1) &&
                        (clue.getIsHidden() == null || clue.getIsHidden() == 0)) {
                    publicClues.add(clue);
                }
            }
            Collections.shuffle(publicClues);
            for (int i = 0; i < Math.min(3, publicClues.size()); i++) {
                gameMapper.insertUnlockedClue(progress.getGameId(), publicClues.get(i).getClueId());
            }
        }
    }

    @Override
    public List<ScriptClue> getUnlockedClues(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        List<ScriptClue> allUnlocked = gameMapper.getUnlockedCluesByGame(gameId);
        List<ScriptClue> result = new ArrayList<>();
        for (ScriptClue clue : allUnlocked) {
            if ((clue.getIsPublic() != null && clue.getIsPublic() == 1) ||
                    (clue.getRoleId() != null && clue.getRoleId().equals(progress.getUserRoleId()))) {
                result.add(clue);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getAiResponses(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        Script script = gameMapper.getScriptById(progress.getScriptId());
        Act act = gameMapper.getActById(progress.getCurrentActId());

        String recentChat = getRecentChatContext(gameId);

        // ✅ 先尝试从数据库获取预设的AI角色内容
        List<RoleActContent> aiContents = gameMapper.getAiRoleContentsByAct(progress.getCurrentActId(), progress.getUserRoleId());
        List<Map<String, String>> responses = new ArrayList<>();

        // ✅ 如果数据库中没有预设内容，则为该剧本的所有其他角色动态生成发言
        if (aiContents.isEmpty()) {
            List<Role> allRoles = gameMapper.getRolesByScriptId(progress.getScriptId());
            for (Role role : allRoles) {
                // 跳过玩家自己扮演的角色
                if (role.getRoleId().equals(progress.getUserRoleId())) {
                    continue;
                }

                // 使用角色的背景信息作为提示词
                String background = role.getBackground() != null ? role.getBackground() : "暂无背景信息";
                String prompt = String.format(
                        "你正在参与剧本杀《%s》，扮演角色是【%s】。当前幕次是：【%s】。\n" +
                                "你的公开人设：%s\n\n" +
                                "【刚刚发生的历史讨论（含DM最新发言）】：\n%s\n\n" +
                                "【任务要求】：\n" +
                                "1. 请以第一人称进行本幕的开场发言（80字左右）。\n" +
                                "2. 极其重要：必须仔细阅读【历史讨论】，你的发言必须承接DM的引导，或者直接回击上一幕其他玩家对你的质疑！表现得像个记仇或警惕的真人玩家。\n" +
                                "3. 如果无缝衔接，你可以抛出一个基于你角色背景的疑问。\n" +
                                "4. 绝不能承认自己是AI。",
                        script.getTitle(), role.getName(), act.getActName(), background, recentChat
                );

                List<Map<String, String>> msgs = new ArrayList<>();
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system"); 
                sysMsg.put("content", prompt);
                msgs.add(sysMsg);

                String aiReply = aiService.generateChatReply(msgs);
                
                // 将生成的发言保存到数据库，下次可以直接使用
                RoleActContent rac = new RoleActContent();
                rac.setScriptId(progress.getScriptId());
                rac.setActId(progress.getCurrentActId());
                rac.setRoleId(role.getRoleId());
                rac.setContent(aiReply);
                try {
                    gameMapper.insertRoleActContent(rac);
                } catch (Exception e) {
                    // 如果已存在则忽略
                }
                
                gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), role.getRoleId(), aiReply);

                Map<String, String> res = new HashMap<>();
                res.put("roleName", role.getName());
                res.put("reply", aiReply);
                responses.add(res);
            }
        } else {
            // 使用数据库中预设的内容
            for (RoleActContent content : aiContents) {
                Role role = gameMapper.getRoleById(content.getRoleId());
                String prompt = String.format(
                        "你正在参与剧本杀《%s》，扮演角色是【%s】。当前幕次是：【%s】。\n" +
                                "你的公开人设：%s\n" +
                                "你本幕的独家机密情报：%s\n\n" +
                                "【刚刚发生的历史讨论（含DM最新发言）】：\n%s\n\n" +
                                "【任务要求】：\n" +
                                "1. 请以第一人称进行本幕的开场发言（80字左右）。\n" +
                                "2. 极其重要：必须仔细阅读【历史讨论】，你的发言必须承接DM的引导，或者直接回击上一幕其他玩家对你的质疑！表现得像个记仇或警惕的真人玩家。\n" +
                                "3. 如果无缝衔接，你可以抛出一个基于你本幕情报的疑问。\n" +
                                "4. 绝不能承认自己是AI。",
                        script.getTitle(), role.getName(), act.getActName(), role.getBackground(), content.getContent(), recentChat
                );

                List<Map<String, String>> msgs = new ArrayList<>();
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system"); 
                sysMsg.put("content", prompt);
                msgs.add(sysMsg);

                String aiReply = aiService.generateChatReply(msgs);
                gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), role.getRoleId(), aiReply);

                Map<String, String> res = new HashMap<>();
                res.put("roleName", role.getName());
                res.put("reply", aiReply);
                responses.add(res);
            }
        }
        
        return responses;
    }

    @Override
    public Map<String, Object> handleAiChat(Integer gameId, List<Map<String, String>> history, String targetRoleName) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) {
            throw new RuntimeException("游戏进度不存在");
        }

        Script script = gameMapper.getScriptById(progress.getScriptId());
        if (script == null) {
            throw new RuntimeException("剧本不存在");
        }

        if (!history.isEmpty()) {
            String latestPlayerMsg = history.get(history.size() - 1).get("content");
            gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), progress.getUserRoleId(), latestPlayerMsg);
        }

        List<RoleActContent> aiContents = gameMapper.getAiRoleContentsByAct(progress.getCurrentActId(), progress.getUserRoleId());

        RoleActContent aiContent = null;
        Role aiRole = null;

        // 如果玩家指定了要对话的角色名，尝试匹配
        if (targetRoleName != null && !targetRoleName.trim().isEmpty() && !aiContents.isEmpty()) {
            String target = targetRoleName.trim();
            for (RoleActContent rac : aiContents) {
                Role r = gameMapper.getRoleById(rac.getRoleId());
                if (r != null && r.getName() != null && r.getName().contains(target)) {
                    aiRole = r;
                    aiContent = rac;
                    break;
                }
            }
            // 如果精确匹配不到，尝试模糊匹配（角色名包含玩家输入 或 玩家输入包含角色名）
            if (aiRole == null) {
                for (RoleActContent rac : aiContents) {
                    Role r = gameMapper.getRoleById(rac.getRoleId());
                    if (r != null && r.getName() != null && target.contains(r.getName())) {
                        aiRole = r;
                        aiContent = rac;
                        break;
                    }
                }
            }
        }

        // 没匹配到或没指定，取第一个（保持向后兼容）
        if (aiContent == null) {
            if (aiContents.isEmpty()) {
                aiRole = new Role();
                aiRole.setName("神秘NPC");
                aiRole.setRoleId(-1);
                aiContent = new RoleActContent();
                aiContent.setContent("你是一个神秘的NPC，正在参与这场剧本杀游戏。请根据玩家的发言进行回应，保持剧情氛围。");
            } else {
                aiContent = aiContents.get(0);
                aiRole = gameMapper.getRoleById(aiContent.getRoleId());
                if (aiRole == null) {
                    aiRole = new Role();
                    aiRole.setName("神秘NPC");
                    aiRole.setRoleId(-1);
                }
            }
        }

        String prompt = String.format(
                "你正在参与剧本杀《%s》，扮演角色【%s】。\n" +
                        "这是你的独家机密情报：%s\n\n" +
                        "【重要交互指令】\n" +
                        "1. 严格保持第一人称扮演，完全沉浸在剧情中，绝不能承认自己是AI或语言模型。\n" +
                        "2. 【核心要求】必须仔细阅读对话记录中玩家的最新发言进行直接关联！如果别人质问你，找借口反驳；如果别人陈述事实，你要表现出惊讶或怀疑！切勿自说自话背剧本。\n" +
                        "3. 保护好你的机密，只有玩家指出致命漏洞时才可以妥协。\n" +
                        "4. 每次回复控制在 30~80 字左右，语气口语化。",
                script.getTitle(), aiRole.getName(), aiContent.getContent());

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        msgs.add(sysMsg);

        List<Map<String, Object>> dbChatRecords = gameMapper.getChatRecords(gameId, progress.getCurrentActId());
        for (Map<String, Object> r : dbChatRecords) {
            Map<String, String> m = new HashMap<>();
            Integer senderRoleId = (Integer) r.get("sender_role_id");
            m.put("role", senderRoleId != null && senderRoleId.equals(aiRole.getRoleId()) ? "assistant" : "user");
            m.put("content", (String) r.get("content"));
            msgs.add(m);
        }

        String aiReply = aiService.generateChatReply(msgs);
        if (aiRole.getRoleId() > 0) {
            gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), aiRole.getRoleId(), aiReply);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("aiName", aiRole.getName());
        result.put("reply", aiReply);
        return result;
    }

    @Override
    public GameProgress nextAct(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) {
            throw new RuntimeException("游戏进度不存在，无法进入下一幕");
        }
        
        List<Act> acts = gameMapper.getActsByScriptId(progress.getScriptId());
        if (acts == null || acts.isEmpty()) {
            throw new RuntimeException("剧本没有配置幕次，无法进入下一幕");
        }

        acts.sort(Comparator.comparingInt(act -> act.getSort() == null ? 0 : act.getSort()));

        Act nextAct = null;
        boolean foundCurrent = false;
        for (Act act : acts) {
            if (foundCurrent) { 
                nextAct = act; 
                break; 
            }
            if (act.getActId().equals(progress.getCurrentActId())) {
                foundCurrent = true;
            }
        }

        if (nextAct != null) {
            progress.setCurrentActId(nextAct.getActId());
            if (progress.getStatus() == null) {
                progress.setStatus("PLAYING");
            }
            if (progress.getPhase() == null) {
                progress.setPhase("DISCUSSION");
            }
            if (progress.getVotedRoleId() == null) {
                progress.setVotedRoleId(0);
            }
            gameMapper.updateGameProgress(progress);
            distributeClues(progress);
        } else {
            progress.setPhase("VOTING");
            if (progress.getStatus() == null) {
                progress.setStatus("PLAYING");
            }
            if (progress.getVotedRoleId() == null) {
                progress.setVotedRoleId(0);
            }
            gameMapper.updateGameProgress(progress);
        }
        return progress;
    }

    @Override
    public Map<String, Object> submitVote(Integer gameId, Integer votedRoleId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) {
            throw new RuntimeException("游戏进度不存在");
        }

        progress.setVotedRoleId(votedRoleId);
        progress.setPhase("END");
        progress.setStatus("end");
        gameMapper.updateGameProgress(progress);

        ScriptEnding ending = gameMapper.getEndingByVote(progress.getScriptId(), votedRoleId);

        if (ending == null && votedRoleId != 0) {
            ending = gameMapper.getEndingByVote(progress.getScriptId(), -1);
        }

        String finalEndingTitle = (ending != null) ? ending.getEndingTitle() : "迷雾重重";
        String finalEndingContent = (ending != null) ? ending.getEndingContent() : "由于天机被蒙蔽，本次游戏未能达成任何已知结局...";

        String truthContent = gameMapper.getScriptTruth(progress.getScriptId());

        try {
            Script script = gameMapper.getScriptById(progress.getScriptId());
            long durationMins = 0;
            if (progress.getCreateTime() != null) {
                durationMins = java.time.Duration.between(progress.getCreateTime(), java.time.LocalDateTime.now()).toMinutes();
            }
            int playDuration = durationMins > 0 ? (int) durationMins : 1;
            String remark = "达成了结局：【" + finalEndingTitle + "】";

            gameMapper.insertPlayRecord(progress.getUserId(), script.getTitle(), script.getScriptId(), playDuration, remark);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("votedRoleId", votedRoleId);
        result.put("endingTitle", finalEndingTitle);
        result.put("endingContent", finalEndingContent);
        result.put("truthContent", truthContent != null ? truthContent : "作者很懒，没有留下复盘真相。");

        return result;
    }

    @Override public void restartGame(Integer g) { gameMapper.deleteGameProgress(g); }
    @Override public GameProgress nextPhase(Integer g) { return null; }

    @Override
    public void recordBrowseHistory(Long userId, Integer scriptId) {
        try {
            int recent = gameMapper.countRecentBrowseByUser(userId, scriptId);
            if (recent > 0) {
                gameMapper.updateRecentBrowseTime(userId, scriptId);
            } else {
                gameMapper.insertBrowseHistory(userId, scriptId);
            }
        } catch (Exception e) {
            // 去重逻辑失败时降级为直接插入
            try {
                gameMapper.insertBrowseHistory(userId, scriptId);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Result<String> submitEvaluation(Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            Long userId = gameMapper.getUserIdByUsername(username);
            if(userId == null) return Result.fail("用户不存在，请重新登录");

            Integer scriptId = (Integer) payload.get("scriptId");
            String scriptName = (String) payload.get("scriptName");
            Integer score = (Integer) payload.get("score");
            String content = (String) payload.get("content");

            gameMapper.insertEvaluateRecord(userId, scriptId, scriptName, score, content);
            return Result.success("评价成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("提交评价失败");
        }
    }

    @Override
    public List<Map<String, Object>> getEvaluationsByScriptId(Integer scriptId) {
        return gameMapper.getEvaluationsByScriptId(scriptId);
    }

    @Override
    public int countEvaluations(Integer scriptId) {
        return gameMapper.countEvaluationsByScriptId(scriptId);
    }

    @Override
    public Double getAverageScore(Integer scriptId) {
        return gameMapper.getAverageScore(scriptId);
    }

    @Override
    public Result<String> toggleFavorite(Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            Long userId = gameMapper.getUserIdByUsername(username);
            if(userId == null) return Result.fail("用户不存在");

            Integer scriptId = Integer.valueOf(payload.get("scriptId").toString());
            gameMapper.createFavoriteTableIfNotExists();

            if (gameMapper.checkFavorite(userId, scriptId) > 0) {
                gameMapper.deleteFavorite(userId, scriptId);
                return Result.success("取消收藏");
            } else {
                gameMapper.insertFavorite(userId, scriptId);
                return Result.success("收藏成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("操作失败");
        }
    }

    @Override
    public List<Integer> getUserFavorites(String username) {
        Long userId = gameMapper.getUserIdByUsername(username);
        if (userId == null) return new ArrayList<>();
        return gameMapper.getUserFavorites(userId);
    }

    @Override
    public Map<String, Object> getDmOpening(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        Script script = gameMapper.getScriptById(progress.getScriptId());
        Act act = gameMapper.getActById(progress.getCurrentActId());

        String recentChat = getRecentChatContext(gameId);

        String prompt = String.format(
                "你是剧本杀《%s》的全智能AI主持人（DM）。当前游戏进入了新幕次：【%s】。\n" +
                        "本幕的公共剧情背景是：%s\n\n" +
                        "【前情回顾与玩家历史讨论】：\n%s\n\n" +
                        "【任务要求】：\n" +
                        "1. 请以DM的身份发表一段推动剧情的转场白（控制在150字以内）。\n" +
                        "2. 极其重要：必须结合【前情回顾】中玩家刚才的讨论！例如，如果玩家刚才怀疑某人，你可以说“你们对xx的怀疑似乎有了新的转折”或者顺着他们的话题引出新的剧情。\n" +
                        "3. 自然地交代新幕次的背景，引导玩家根据新线索继续推理。\n" +
                        "4. 语气保持神秘、悬疑，绝对不能自称是AI，绝不直接剧透真相。",
                script.getTitle(), act.getActName(), act.getPublicContent(), recentChat
        );

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        msgs.add(sysMsg);

        String dmReply = aiService.generateChatReply(msgs);
        gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), 0, dmReply);

        Map<String, Object> res = new HashMap<>();
        res.put("roleName", "🎤 DM 主持人");
        res.put("reply", dmReply);
        return res;
    }

    @Override
    public Map<String, Object> getDmHelp(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        Script script = gameMapper.getScriptById(progress.getScriptId());
        Act act = gameMapper.getActById(progress.getCurrentActId());

        List<ScriptClue> clues = getUnlockedClues(gameId);
        StringBuilder cluesText = new StringBuilder();
        for(ScriptClue c : clues) {
            cluesText.append(c.getClueName()).append(":").append(c.getClueDesc()).append("; ");
        }

        List<Map<String, Object>> chatRecords = gameMapper.getChatRecords(gameId, progress.getCurrentActId());
        StringBuilder chatText = new StringBuilder();
        int start = Math.max(0, chatRecords.size() - 10);
        for(int i = start; i < chatRecords.size(); i++) {
            chatText.append(chatRecords.get(i).get("content")).append(" | ");
        }

        String prompt = String.format(
                "你是剧本杀《%s》的AI主持人（DM）。玩家当前在【%s】陷入了僵局，点击了求助按钮。\n" +
                        "他们目前掌握的线索有：%s\n" +
                        "他们最近的讨论记录是：%s\n\n" +
                        "【任务】请作为DM，分析他们的讨论，给出一个模糊的推理方向，或者指出他们忽略的盲点，引导他们继续推理。\n" +
                        "【绝对禁忌】绝不能直接剧透真相或说出凶手！语气要像一个神秘的法官，字数控制在80字左右。",
                script.getTitle(), act.getActName(), cluesText.toString(), chatText.toString()
        );

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        msgs.add(sysMsg);

        String dmReply = aiService.generateChatReply(msgs);
        gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), 0, dmReply);

        Map<String, Object> res = new HashMap<>();
        res.put("roleName", "🎤 DM 主持人");
        res.put("reply", dmReply);
        return res;
    }

    @Override
    public Map<String, Object> investigateScene(Integer gameId, String actionText) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) throw new RuntimeException("游戏进度异常");

        List<ScriptClue> hiddenClues = gameMapper.getUndiscoveredHiddenClues(progress.getScriptId(), progress.getCurrentActId(), gameId);

        Map<String, Object> result = new HashMap<>();

        if (hiddenClues == null || hiddenClues.isEmpty()) {
            result.put("found", false);
            result.put("reply", getNoClueReplyByAction(gameId, actionText));
            return result;
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个剧本杀的现场搜证裁判。玩家当前的搜查动作是：【").append(actionText).append("】。\n");
        promptBuilder.append("当前现场存在的隐藏线索及解锁条件如下：\n");
        for (ScriptClue clue : hiddenClues) {
            promptBuilder.append("[ID: ").append(clue.getClueId())
                    .append(", 解锁条件: ").append(clue.getUnlockCondition() != null ? clue.getUnlockCondition() : "无").append("]\n");
        }
        promptBuilder.append("\n【任务要求】：\n" +
                "1. 请判断玩家的动作是否符合上述任意一个条件？（允许近义词或相关动作的宽泛匹配，以鼓励玩家）。\n" +
                "2. 必须严格只输出一段合法的 JSON，不要带有代码标记（如 ```json）。\n" +
                "3. 如果满足条件，输出：{\"found\": true, \"clue_id\": 对应的ID数字, \"reply\": \"你发现了...的描述文本\"}\n" +
                "4. 如果不满足，输出：{\"found\": false, \"clue_id\": null, \"reply\": \"你仔细搜查了...但一无所获。\"}\n" +
                "注意：如果玩家毫无头绪，请在 reply 中给予场景方位上的一点暗示。");

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", promptBuilder.toString());
        msgs.add(sysMsg);

        String aiResponse = aiService.generateChatReply(msgs);

        try {
            String cleanJson = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(cleanJson);

            boolean found = rootNode.path("found").asBoolean(false);
            String reply = rootNode.path("reply").asText("搜查结束。");

            result.put("found", found);
            result.put("reply", found ? reply : getNoClueReplyByAction(gameId, actionText));

            if (found && rootNode.has("clue_id") && !rootNode.path("clue_id").isNull()) {
                long foundClueId = rootNode.path("clue_id").asLong();
                gameMapper.insertUnlockedClue(gameId, foundClueId);

                for (ScriptClue clue : hiddenClues) {
                    if (clue.getClueId().equals(foundClueId)) {
                        result.put("clueData", clue);
                        break;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("found", false);
            result.put("reply", getNoClueReplyByAction(gameId, actionText));
            return result;
        }
    }

    private String getNoClueReplyByAction(Integer gameId, String action) {
        try {
            String reply = gameMapper.getNoClueReplyByAction(gameId, action);
            return reply != null ? reply : "🤖 裁判反馈：你仔细搜查了一番，但这里似乎已经没有什么有价值的线索了。";
        } catch (Exception e) {
            return getDefaultReplyForAction(action);
        }
    }

    private String getDefaultReplyForAction(String action) {
        Map<String, String> defaultReplies = new HashMap<>();
        defaultReplies.put("检查桌面", "🤖 裁判反馈：\n你仔细检查了桌面，但没有什么有价值的线索。");
        defaultReplies.put("检查抽屉", "🤖 裁判反馈：\n你拉开抽屉翻了翻，里面空空如也。");
        defaultReplies.put("检查柜子", "🤖 裁判反馈：\n你打开柜子看了看，什么都没有。");
        defaultReplies.put("翻看书籍", "🤖 裁判反馈：\n你翻阅了书籍，没有发现夹带的东西。");
        defaultReplies.put("查看墙壁", "🤖 裁判反馈：\n你检查了墙壁，没有暗格或异常。");
        defaultReplies.put("检查地板", "🤖 裁判反馈：\n你仔细检查了地板，没有发现隐藏的东西。");
        defaultReplies.put("搜查衣物", "🤖 裁判反馈：\n你搜查了衣物口袋，没有找到什么。");
        defaultReplies.put("检查行李", "🤖 裁判反馈：\n你检查了行李，里面没有线索。");
        defaultReplies.put("观察周围", "🤖 裁判反馈：\n你环顾四周，没有发现异常。");
        defaultReplies.put("与人交谈", "🤖 裁判反馈：\n你尝试与对方交谈，但没有什么收获。");
        return defaultReplies.getOrDefault(action, "🤖 裁判反馈：你仔细搜查了一番，但这里似乎已经没有什么有价值的线索了。");
    }

    @jakarta.annotation.PostConstruct
    public void initDatabaseTables() {
        try {
            gameMapper.dropInvestigateActionTable();
            gameMapper.createInvestigateActionTable();

            String[][] actionReplies = {
                {"检查桌面", "🤖 裁判反馈：\n你仔细检查了桌面，但没有什么有价值的线索。"},
                {"检查抽屉", "🤖 裁判反馈：\n你拉开抽屉翻了翻，里面空空如也。"},
                {"检查柜子", "🤖 裁判反馈：\n你打开柜子看了看，什么都没有。"},
                {"翻看书籍", "🤖 裁判反馈：\n你翻阅了书籍，没有发现夹带的东西。"},
                {"查看墙壁", "🤖 裁判反馈：\n你检查了墙壁，没有暗格或异常。"},
                {"检查地板", "🤖 裁判反馈：\n你仔细检查了地板，没有发现隐藏的东西。"},
                {"搜查衣物", "🤖 裁判反馈：\n你搜查了衣物口袋，没有找到什么。"},
                {"检查行李", "🤖 裁判反馈：\n你检查了行李，里面没有线索。"},
                {"观察周围", "🤖 裁判反馈：\n你环顾四周，没有发现异常。"},
                {"与人交谈", "🤖 裁判反馈：\n你尝试与对方交谈，但没有什么收获。"}
            };
            for (int i = 0; i < actionReplies.length; i++) {
                gameMapper.insertInvestigateAction(0, actionReplies[i][0], actionReplies[i][1], i);
            }
            System.out.println("✅ 搜证动作和固定回复数据初始化完成");
        } catch (Exception e) {
            System.err.println("❌ 初始化数据库表失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllInvestigateActions() {
        try {
            return gameMapper.getInvestigateActionsByScriptId(0);
        } catch (Exception e) {
            String[] defaultActions = {"检查桌面", "检查抽屉", "检查柜子", "翻看书籍", "查看墙壁", "检查地板", "搜查衣物", "检查行李", "观察周围", "与人交谈"};
            return Arrays.asList(defaultActions);
        }
    }

    public List<String> getInvestigateActionsByGameId(Integer gameId) {
        try {
            GameProgress progress = gameMapper.getGameProgress(gameId);
            if (progress == null) return getAllInvestigateActions();
            return gameMapper.getInvestigateActionsByScriptId(progress.getScriptId());
        } catch (Exception e) {
            return getAllInvestigateActions();
        }
    }

    @Override
    public List<String> getSceneImages(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) return new ArrayList<>();

        List<ScriptClue> allClues = gameMapper.getAllCluesByScriptId(progress.getScriptId());
        Set<String> urls = new LinkedHashSet<>();

        for (ScriptClue clue : allClues) {
            if (clue.getUnlockChapterId() != null && clue.getUnlockChapterId().equals(Long.valueOf(progress.getCurrentActId()))) {
                if (clue.getIsHidden() != null && clue.getIsHidden() == 1) {
                    String url = clue.getSceneImageUrl();
                    if (url != null && !url.trim().isEmpty()) {
                        urls.add(url);
                    }
                }
            }
        }
        return new ArrayList<>(urls);
    }

    @Override
    public Map<String, Object> getInvestigateHint(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) throw new RuntimeException("游戏进度异常");

        List<ScriptClue> hiddenClues = gameMapper.getUndiscoveredHiddenClues(progress.getScriptId(), progress.getCurrentActId(), gameId);
        Map<String, Object> result = new HashMap<>();

        if (hiddenClues == null || hiddenClues.isEmpty()) {
            result.put("reply", "🎤 DM 提示：各位大侦探，这个案发现场已经被你们搜得底朝天了，去看看别的公共情报，或者和其他人聊聊吧！");
            return result;
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是剧本杀的 DM（主持人）。玩家目前在现场搜证时卡关了，向你求助。\n");
        promptBuilder.append("当前现场还有以下隐藏线索没有被发现（包含它们的触发条件）：\n");
        for (ScriptClue clue : hiddenClues) {
            promptBuilder.append("- 线索: ").append(clue.getClueName())
                    .append(", 藏匿条件: ").append(clue.getUnlockCondition() != null ? clue.getUnlockCondition() : "未知").append("\n");
        }
        promptBuilder.append("\n【任务要求】：\n" +
                "1. 请以 DM 的口吻，给玩家一个隐晦的场景方位暗示。\n" +
                "2. 绝对不能直接说出线索的名字，也绝对不能直接把触发条件原话照念！\n" +
                "3. 语气要充满悬疑感，稍微有些神秘。字数控制在 60 字以内。\n" +
                "4. 示例：‘大家似乎忽略了死者平时最喜欢待的那个角落...’ 或 ‘有些秘密，可能被压在沉重的东西下面...’");

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", promptBuilder.toString());
        msgs.add(sysMsg);

        String aiResponse = aiService.generateChatReply(msgs);
        result.put("reply", "🎤 DM 提示：" + aiResponse);
        return result;
    }

    @Override
    public List<ScriptSeries> getAllSeries() { return gameMapper.getAllSeries(); }

    @Override
    public ScriptSeries createSeries(Map<String, Object> payload) {
        ScriptSeries series = new ScriptSeries();
        series.setSeriesName((String) payload.get("seriesName"));
        series.setSeriesDesc((String) payload.get("seriesDesc"));
        series.setCoverUrl((String) payload.get("coverUrl"));
        series.setBackgroundUrl((String) payload.get("backgroundUrl"));
        gameMapper.insertSeries(series);
        return series;
    }

    @Override
    public ScriptSeries updateSeries(Map<String, Object> payload) {
        ScriptSeries series = new ScriptSeries();
        series.setSeriesId(Integer.valueOf(payload.get("seriesId").toString()));
        series.setSeriesName((String) payload.get("seriesName"));
        series.setSeriesDesc((String) payload.get("seriesDesc"));
        series.setCoverUrl((String) payload.get("coverUrl"));
        series.setBackgroundUrl((String) payload.get("backgroundUrl"));
        gameMapper.updateSeries(series);
        return series;
    }

    @Override
    public ScriptSeries getSeriesById(Integer seriesId) {
        return gameMapper.getSeriesById(seriesId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"scriptList", "roles", "acts"}, allEntries = true)
    public void deleteSeriesById(Integer seriesId) {
        gameMapper.deleteSeriesById(seriesId);
    }

    @Override
    public Map<String, Object> getSeriesDetail(Integer seriesId) {
        Map<String, Object> result = new HashMap<>();
        result.put("seriesInfo", gameMapper.getSeriesById(seriesId));
        result.put("scripts", gameMapper.getScriptsBySeriesId(seriesId));
        return result;
    }

    @Override
    public Map<String, Object> getCampaignProgress(Integer seriesId, String username) {
        Long userId = gameMapper.getUserIdByUsername(username);
        List<Script> scripts = gameMapper.getScriptsBySeriesId(seriesId);
        List<Integer> playedScriptIds = (userId != null) ? gameMapper.getUserPlayedScriptIds(userId) : new ArrayList<>();

        List<Map<String, Object>> levelList = new ArrayList<>();
        boolean previousCompleted = true;

        for (int i = 0; i < scripts.size(); i++) {
            Script s = scripts.get(i);
            Map<String, Object> node = new HashMap<>();
            node.put("script", s);
            node.put("levelIndex", s.getSeriesOrder() != null && s.getSeriesOrder() > 0 ? s.getSeriesOrder() : i + 1);

            boolean isCompleted = playedScriptIds.contains(s.getScriptId());
            boolean isUnlocked = previousCompleted || isCompleted;

            node.put("status", isCompleted ? "completed" : (isUnlocked ? "unlocked" : "locked"));
            levelList.add(node);
            previousCompleted = isCompleted;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("seriesInfo", gameMapper.getSeriesById(seriesId));
        res.put("levels", levelList);
        return res;
    }

    @Override
    public List<ScriptArchitecture> getArchitecturesByScriptId(Integer scriptId) {
        return gameMapper.getArchitecturesByScriptId(scriptId);
    }
}