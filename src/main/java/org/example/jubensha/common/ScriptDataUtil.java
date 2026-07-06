package org.example.jubensha.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.jubensha.entity.*;
import org.example.jubensha.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧本数据导出导入工具类
 * 用于复制和备份剧本数据
 */
@Component
public class ScriptDataUtil {

    @Autowired
    private GameMapper gameMapper;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * 导出单个剧本的完整数据为JSON
     */
    public String exportScriptToJson(Integer scriptId) throws IOException {
        Map<String, Object> scriptData = new HashMap<>();

        // 获取剧本基本信息
        Script script = gameMapper.getScriptById(scriptId);
        scriptData.put("script", script);

        // 获取角色数据
        List<Role> roles = gameMapper.getRolesByScriptId(scriptId);
        scriptData.put("roles", roles);

        // 获取幕次数据
        List<Act> acts = gameMapper.getActsByScriptId(scriptId);
        scriptData.put("acts", acts);

        // 获取角色剧情数据
        List<RoleActContent> roleContents = gameMapper.getAllContentsByScriptId(scriptId);
        scriptData.put("roleContents", roleContents);

        // 获取线索数据
        List<ScriptClue> clues = gameMapper.getAllCluesByScriptId(scriptId);
        scriptData.put("clues", clues);

        // 获取结局数据
        List<ScriptEnding> endings = gameMapper.getEndingsByScriptId(scriptId);
        scriptData.put("endings", endings);

        // 获取建筑/场景数据
        List<ScriptArchitecture> architectures = gameMapper.getArchitecturesByScriptId(scriptId);
        scriptData.put("architectures", architectures);

        return objectMapper.writeValueAsString(scriptData);
    }

    /**
     * 导出剧本数据到文件
     */
    public void exportScriptToFile(Integer scriptId, String filePath) throws IOException {
        String json = exportScriptToJson(scriptId);
        Files.write(Paths.get(filePath), json.getBytes("UTF-8"));
    }

    /**
     * 从JSON数据导入剧本
     */
    @SuppressWarnings("unchecked")
    public Integer importScriptFromJson(String json) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, Map.class);

        // 读取剧本基本信息
        Map<String, Object> scriptMap = (Map<String, Object>) data.get("script");
        Script script = mapToScript(scriptMap);
        
        // 设置userId为默认用户
        script.setUserId(1L);
        gameMapper.insertScript(script);
        Integer newScriptId = script.getScriptId();

        // 读取角色数据
        List<Map<String, Object>> rolesList = (List<Map<String, Object>>) data.get("roles");
        Map<Integer, Integer> roleIdMapping = new HashMap<>();
        for (Map<String, Object> roleMap : rolesList) {
            Role oldRole = mapToRole(roleMap);
            Integer oldRoleId = oldRole.getRoleId();
            oldRole.setScriptId(newScriptId);
            oldRole.setRoleId(null); // 重置ID，让数据库自动生成
            gameMapper.insertRole(oldRole);
            roleIdMapping.put(oldRoleId, oldRole.getRoleId());
        }

        // 读取幕次数据
        List<Map<String, Object>> actsList = (List<Map<String, Object>>) data.get("acts");
        Map<Integer, Integer> actIdMapping = new HashMap<>();
        for (Map<String, Object> actMap : actsList) {
            Act oldAct = mapToAct(actMap);
            Integer oldActId = oldAct.getActId();
            oldAct.setScriptId(newScriptId);
            oldAct.setActId(null); // 重置ID
            gameMapper.insertAct(oldAct);
            actIdMapping.put(oldActId, oldAct.getActId());
        }

        // 读取角色剧情数据
        List<Map<String, Object>> contentsList = (List<Map<String, Object>>) data.get("roleContents");
        for (Map<String, Object> contentMap : contentsList) {
            RoleActContent oldContent = mapToRoleActContent(contentMap);
            // 更新ID映射
            oldContent.setScriptId(newScriptId);
            oldContent.setActId(actIdMapping.get(oldContent.getActId()));
            oldContent.setRoleId(roleIdMapping.get(oldContent.getRoleId()));
            oldContent.setId(null);
            gameMapper.insertRoleActContent(oldContent);
        }

        // 读取线索数据
        List<Map<String, Object>> cluesList = (List<Map<String, Object>>) data.get("clues");
        for (Map<String, Object> clueMap : cluesList) {
            ScriptClue oldClue = mapToScriptClue(clueMap);
            oldClue.setScriptId(newScriptId.longValue());
            if (oldClue.getUnlockChapterId() != null) {
                oldClue.setUnlockChapterId(actIdMapping.get(oldClue.getUnlockChapterId().intValue()).longValue());
            }
            if (oldClue.getRoleId() != null) {
                oldClue.setRoleId(roleIdMapping.get(oldClue.getRoleId()));
            }
            oldClue.setClueId(null);
            gameMapper.insertScriptClue(oldClue);
        }

        // 读取结局数据
        List<Map<String, Object>> endingsList = (List<Map<String, Object>>) data.get("endings");
        for (Map<String, Object> endingMap : endingsList) {
            ScriptEnding oldEnding = mapToScriptEnding(endingMap);
            oldEnding.setScriptId(newScriptId);
            if (oldEnding.getVotedRoleId() != null && oldEnding.getVotedRoleId() > 0) {
                oldEnding.setVotedRoleId(roleIdMapping.get(oldEnding.getVotedRoleId()));
            }
            oldEnding.setEndingId(null);
            gameMapper.insertScriptEnding(oldEnding);
        }

        // 读取建筑/场景数据
        if (data.containsKey("architectures")) {
            List<Map<String, Object>> archList = (List<Map<String, Object>>) data.get("architectures");
            for (Map<String, Object> archMap : archList) {
                ScriptArchitecture oldArch = mapToScriptArchitecture(archMap);
                oldArch.setScriptId(newScriptId);
                oldArch.setArchId(null);
                gameMapper.insertArchitecture(oldArch);
            }
        }

        return newScriptId;
    }

    /**
     * 从文件导入剧本
     */
    public Integer importScriptFromFile(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        return importScriptFromJson(json);
    }

    /**
     * 导出所有剧本
     */
    public List<String> exportAllScriptsToJson() throws IOException {
        List<Script> scripts = gameMapper.getScriptList();
        List<String> jsons = new ArrayList<>();
        for (Script script : scripts) {
            jsons.add(exportScriptToJson(script.getScriptId()));
        }
        return jsons;
    }

    // ================== 私有辅助方法 ==================

    private Script mapToScript(Map<String, Object> map) {
        Script script = new Script();
        if (map.get("scriptId") != null) {
            script.setScriptId((Integer) map.get("scriptId"));
        }
        script.setTitle((String) map.get("title"));
        script.setIntro((String) map.get("intro"));
        script.setPlayerCount((Integer) map.get("playerCount"));
        script.setDifficulty((String) map.get("difficulty"));
        script.setCoverUrl((String) map.get("coverUrl"));
        script.setTags((String) map.get("tags"));
        script.setTruthContent((String) map.get("truthContent"));
        return script;
    }

    private Role mapToRole(Map<String, Object> map) {
        Role role = new Role();
        if (map.get("roleId") != null) {
            role.setRoleId((Integer) map.get("roleId"));
        }
        role.setScriptId((Integer) map.get("scriptId"));
        role.setName((String) map.get("name"));
        role.setBackground((String) map.get("background"));
        if (map.get("isAi") != null) {
            role.setIsAi((Integer) map.get("isAi"));
        }
        role.setAvatar((String) map.get("avatar"));
        return role;
    }

    private Act mapToAct(Map<String, Object> map) {
        Act act = new Act();
        if (map.get("actId") != null) {
            act.setActId((Integer) map.get("actId"));
        }
        act.setScriptId((Integer) map.get("scriptId"));
        act.setActName((String) map.get("actName"));
        act.setSort((Integer) map.get("sort"));
        act.setPublicContent((String) map.get("publicContent"));
        return act;
    }

    private RoleActContent mapToRoleActContent(Map<String, Object> map) {
        RoleActContent content = new RoleActContent();
        if (map.get("id") != null) {
            content.setId((Integer) map.get("id"));
        }
        content.setScriptId((Integer) map.get("scriptId"));
        content.setActId((Integer) map.get("actId"));
        content.setRoleId((Integer) map.get("roleId"));
        content.setContent((String) map.get("content"));
        return content;
    }

    private ScriptClue mapToScriptClue(Map<String, Object> map) {
        ScriptClue clue = new ScriptClue();
        if (map.get("clueId") != null) {
            clue.setClueId((Long) map.get("clueId"));
        }
        if (map.get("scriptId") != null) {
            clue.setScriptId(((Number) map.get("scriptId")).longValue());
        }
        clue.setClueName((String) map.get("clueName"));
        clue.setClueDesc((String) map.get("clueDesc"));
        if (map.get("isPublic") != null) {
            clue.setIsPublic((Integer) map.get("isPublic"));
        }
        if (map.get("roleId") != null) {
            clue.setRoleId((Integer) map.get("roleId"));
        }
        if (map.get("unlockChapterId") != null) {
            clue.setUnlockChapterId(((Number) map.get("unlockChapterId")).longValue());
        }
        if (map.get("isHidden") != null) {
            clue.setIsHidden((Integer) map.get("isHidden"));
        }
        clue.setUnlockCondition((String) map.get("unlockCondition"));
        clue.setSceneImageUrl((String) map.get("sceneImageUrl"));
        return clue;
    }

    private ScriptEnding mapToScriptEnding(Map<String, Object> map) {
        ScriptEnding ending = new ScriptEnding();
        if (map.get("endingId") != null) {
            ending.setEndingId((Integer) map.get("endingId"));
        }
        ending.setScriptId((Integer) map.get("scriptId"));
        if (map.get("votedRoleId") != null) {
            ending.setVotedRoleId((Integer) map.get("votedRoleId"));
        }
        ending.setEndingTitle((String) map.get("endingTitle"));
        ending.setEndingContent((String) map.get("endingContent"));
        return ending;
    }

    private ScriptArchitecture mapToScriptArchitecture(Map<String, Object> map) {
        ScriptArchitecture arch = new ScriptArchitecture();
        if (map.get("archId") != null) {
            arch.setArchId((Integer) map.get("archId"));
        }
        arch.setScriptId((Integer) map.get("scriptId"));
        arch.setArchName((String) map.get("archName"));
        arch.setImageUrl((String) map.get("imageUrl"));
        arch.setArchDesc((String) map.get("archDesc"));
        return arch;
    }
}
