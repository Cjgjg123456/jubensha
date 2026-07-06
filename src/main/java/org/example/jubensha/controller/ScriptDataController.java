package org.example.jubensha.controller;

import org.example.jubensha.common.Result;
import org.example.jubensha.common.ScriptDataUtil;
import org.example.jubensha.entity.Script;
import org.example.jubensha.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧本数据导入导出控制器
 */
@RestController
@RequestMapping("/api/script-data")
@CrossOrigin(origins = "*")
public class ScriptDataController {

    @Autowired
    private ScriptDataUtil scriptDataUtil;

    @Autowired
    private GameMapper gameMapper;

    /**
     * 获取所有剧本列表（用于选择要导出的剧本）
     */
    @GetMapping("/scripts")
    public Result<List<Script>> getAllScripts() {
        try {
            List<Script> scripts = gameMapper.getScriptList();
            return Result.success(scripts);
        } catch (Exception e) {
            return Result.fail("获取剧本列表失败: " + e.getMessage());
        }
    }

    /**
     * 导出单个剧本为JSON
     */
    @GetMapping("/export/{scriptId}")
    public Result<Map<String, Object>> exportScript(@PathVariable Integer scriptId) {
        try {
            String json = scriptDataUtil.exportScriptToJson(scriptId);
            Script script = gameMapper.getScriptById(scriptId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("scriptName", script.getTitle());
            result.put("scriptId", scriptId);
            result.put("data", json);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("导出剧本失败: " + e.getMessage());
        }
    }

    /**
     * 导出剧本到文件并下载
     */
    @GetMapping(value = "/download/{scriptId}", produces = "application/json")
    public String downloadScript(@PathVariable Integer scriptId) {
        try {
            return scriptDataUtil.exportScriptToJson(scriptId);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 从JSON字符串导入剧本
     */
    @PostMapping("/import/json")
    public Result<Map<String, Object>> importScriptFromJson(@RequestBody Map<String, String> request) {
        try {
            String jsonData = request.get("data");
            if (jsonData == null || jsonData.trim().isEmpty()) {
                return Result.fail("剧本数据不能为空");
            }
            
            Integer newScriptId = scriptDataUtil.importScriptFromJson(jsonData);
            Script newScript = gameMapper.getScriptById(newScriptId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("scriptId", newScriptId);
            result.put("scriptName", newScript.getTitle());
            result.put("message", "剧本导入成功！");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("导入剧本失败: " + e.getMessage());
        }
    }

    /**
     * 从文件导入剧本
     */
    @PostMapping("/import/file")
    public Result<Map<String, Object>> importScriptFromFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.fail("请选择要导入的文件");
            }
            
            String jsonData = new String(file.getBytes(), StandardCharsets.UTF_8);
            Integer newScriptId = scriptDataUtil.importScriptFromJson(jsonData);
            Script newScript = gameMapper.getScriptById(newScriptId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("scriptId", newScriptId);
            result.put("scriptName", newScript.getTitle());
            result.put("message", "剧本导入成功！");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("导入剧本失败: " + e.getMessage());
        }
    }

    /**
     * 复制剧本（基于已有剧本创建副本）
     */
    @PostMapping("/copy/{scriptId}")
    public Result<Map<String, Object>> copyScript(@PathVariable Integer scriptId) {
        try {
            // 先导出
            String json = scriptDataUtil.exportScriptToJson(scriptId);
            // 再导入
            Integer newScriptId = scriptDataUtil.importScriptFromJson(json);
            
            // 修改剧本名称，加上副本标识
            Script newScript = gameMapper.getScriptById(newScriptId);
            newScript.setTitle(newScript.getTitle() + " (副本)");
            gameMapper.updateScript(newScript);
            
            Map<String, Object> result = new HashMap<>();
            result.put("scriptId", newScriptId);
            result.put("scriptName", newScript.getTitle());
            result.put("message", "剧本复制成功！");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("复制剧本失败: " + e.getMessage());
        }
    }

    /**
     * 导出所有剧本
     */
    @GetMapping("/export-all")
    public Result<Map<String, Object>> exportAllScripts() {
        try {
            List<String> allScriptsJson = scriptDataUtil.exportAllScriptsToJson();
            List<Script> allScripts = gameMapper.getScriptList();
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", allScriptsJson.size());
            result.put("scripts", allScripts);
            result.put("data", allScriptsJson);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("导出所有剧本失败: " + e.getMessage());
        }
    }
}
