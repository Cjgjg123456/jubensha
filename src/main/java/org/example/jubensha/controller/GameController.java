package org.example.jubensha.controller;

import org.example.jubensha.common.Result;
import org.example.jubensha.entity.*;
import org.example.jubensha.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @GetMapping("/scripts")
    public Result<List<Script>> getScripts() { return Result.success(gameService.getScriptList()); }

    @GetMapping("/scripts/by-user/{userId}")
    public Result<List<Script>> getScriptsByUserId(@PathVariable Long userId) { return Result.success(gameService.getScriptsByUserId(userId)); }

    @GetMapping("/roles")
    public Result<List<Role>> getRoles(@RequestParam Integer scriptId) { return Result.success(gameService.getRolesByScriptId(scriptId)); }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "media_" + UUID.randomUUID().toString().replace("-", "") + ext;
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(uploadPath + fileName);
            file.transferTo(dest);
            return Result.success("/uploads/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("上传失败");
        }
    }

    @PostMapping("/upload/voice")
    public Result<String> uploadVoice(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.fail("语音文件为空");
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.fail("语音文件超过10MB限制");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT)
                    : ".webm";
            Set<String> allowedExts = Set.of(".webm", ".wav", ".mp3", ".m4a", ".ogg", ".mp4", ".flac");
            if (!allowedExts.contains(ext)) {
                return Result.fail("不支持的语音格式");
            }

            String contentType = file.getContentType();
            if (contentType != null
                    && !contentType.toLowerCase(Locale.ROOT).startsWith("audio/")
                    && !"application/octet-stream".equalsIgnoreCase(contentType)) {
                return Result.fail("上传文件不是音频类型");
            }

            Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path voiceDir = baseDir.resolve("voices").normalize();
            Files.createDirectories(voiceDir);

            String fileName = "voice_" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path dest = voiceDir.resolve(fileName).normalize();
            if (!dest.startsWith(baseDir)) {
                return Result.fail("语音上传路径非法");
            }

            file.transferTo(dest.toFile());
            return Result.success("/uploads/voices/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("语音上传失败");
        }
    }

    @PostMapping("/createScript")
    public Result<Integer> createScript(@RequestBody Map<String, Object> payload) { return Result.success(gameService.createNewScript(payload)); }

    @PostMapping("/updateScript")
    public Result<Integer> updateScript(@RequestBody Map<String, Object> payload) { return Result.success(gameService.updateExistingScript(payload)); }

    @DeleteMapping("/script/{id}")
    public Result<String> deleteScript(@PathVariable("id") Integer id, @RequestParam String username) { 
        gameService.deleteScriptById(id, username); 
        return Result.success("删除成功"); 
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> getScriptDetail(@RequestParam Integer scriptId) { return Result.success(gameService.getScriptFullDetail(scriptId)); }

    @PostMapping("/start")
    public Result<GameProgress> startGame(@RequestParam Long userId, @RequestParam Integer scriptId, @RequestParam Integer roleId) {
        try {
            return Result.success(gameService.startGame(userId, scriptId, roleId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("启动游戏失败：" + e.getMessage());
        }
    }

    @GetMapping("/content")
    public Result<Map<String, Object>> getContent(@RequestParam Integer gameId, @RequestParam(required = false) Integer actId) { return Result.success(gameService.getActContentWithCheck(gameId, actId)); }

    @GetMapping("/clues")
    public Result<List<ScriptClue>> getClues(@RequestParam Integer gameId) { return Result.success(gameService.getUnlockedClues(gameId)); }

    @PostMapping("/finishReading")
    public Result<List<Map<String, String>>> finishReading(@RequestParam Integer gameId) { return Result.success(gameService.getAiResponses(gameId)); }

    @PostMapping("/chat")
    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        try {
            Integer gameId = (Integer) payload.get("gameId");
            List<Map<String, String>> history = (List<Map<String, String>>) payload.get("history");
            String targetRoleName = (String) payload.get("targetRoleName");
            return Result.success(gameService.handleAiChat(gameId, history, targetRoleName));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("大模型服务异常：" + e.getMessage());
        }
    }

    @PostMapping("/nextAct")
    public Result<GameProgress> nextAct(@RequestParam Integer gameId) { return Result.success(gameService.nextAct(gameId)); }

    @PostMapping("/submitVote")
    public Result<Map<String, Object>> submitVote(@RequestBody Map<String, Object> payload) {
        try {
            Integer gameId = Integer.valueOf(payload.get("gameId").toString());
            Integer votedRoleId = Integer.valueOf(payload.get("votedRoleId").toString());
            return Result.success(gameService.submitVote(gameId, votedRoleId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("投票失败：" + e.getMessage());
        }
    }

    @PostMapping("/evaluate")
    public Result<String> evaluate(@RequestBody Map<String, Object> payload) { return gameService.submitEvaluation(payload); }

    @GetMapping("/evaluations")
    public Result<Map<String, Object>> getEvaluations(@RequestParam Integer scriptId) {
        Map<String, Object> result = new HashMap<>();
        result.put("evaluations", gameService.getEvaluationsByScriptId(scriptId));
        result.put("count", gameService.countEvaluations(scriptId));
        result.put("averageScore", gameService.getAverageScore(scriptId));
        return Result.success(result);
    }

    @PostMapping("/favorite")
    public Result<String> favorite(@RequestBody Map<String, Object> payload) { return gameService.toggleFavorite(payload); }

    @GetMapping("/favorites")
    public Result<List<Integer>> getFavorites(@RequestParam String username) { return Result.success(gameService.getUserFavorites(username)); }

    @PostMapping("/dm/opening")
    public Result<Map<String, Object>> dmOpening(@RequestParam Integer gameId) { return Result.success(gameService.getDmOpening(gameId)); }

    @PostMapping("/dm/help")
    public Result<Map<String, Object>> dmHelp(@RequestParam Integer gameId) { return Result.success(gameService.getDmHelp(gameId)); }

    @PostMapping("/investigate")
    public Result<Map<String, Object>> investigate(@RequestBody Map<String, Object> payload) {
        try {
            Integer gameId = Integer.valueOf(payload.get("gameId").toString());
            String actionText = (String) payload.get("actionText");
            if (actionText == null || actionText.trim().isEmpty()) return Result.fail("搜查动作不能为空");
            return Result.success(gameService.investigateScene(gameId, actionText));
        } catch (Exception e) {
            return Result.fail("搜证失败：" + e.getMessage());
        }
    }

    @GetMapping("/sceneImages")
    public Result<List<String>> getSceneImages(@RequestParam Integer gameId) { return Result.success(gameService.getSceneImages(gameId)); }

    @GetMapping("/investigate/actions")
    public Result<List<String>> getAllInvestigateActions() {
        return Result.success(gameService.getAllInvestigateActions());
    }

    @GetMapping("/investigate/actions/byGame")
    public Result<List<String>> getInvestigateActionsByGame(@RequestParam Integer gameId) {
        return Result.success(gameService.getInvestigateActionsByGameId(gameId));
    }

    @PostMapping("/investigate/hint")
    public Result<Map<String, Object>> getInvestigateHint(@RequestParam Integer gameId) {
        try { return Result.success(gameService.getInvestigateHint(gameId)); }
        catch (Exception e) { return Result.fail("DM 提示获取失败：" + e.getMessage()); }
    }

    @GetMapping("/series/list")
    public Result<List<ScriptSeries>> getAllSeries() {
        return Result.success(gameService.getAllSeries());
    }

    @PostMapping("/series/create")
    public Result<ScriptSeries> createSeries(@RequestBody Map<String, Object> payload) {
        return Result.success(gameService.createSeries(payload));
    }

    @PostMapping("/series/update")
    public Result<ScriptSeries> updateSeries(@RequestBody Map<String, Object> payload) {
        try {
            return Result.success(gameService.updateSeries(payload));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("更新系列失败: " + e.getMessage());
        }
    }

    @GetMapping("/series/detail")
    public Result<ScriptSeries> getSeriesDetail(@RequestParam Integer seriesId) {
        ScriptSeries series = gameService.getSeriesById(seriesId);
        if (series != null) {
            return Result.success(series);
        }
        return Result.fail("系列不存在");
    }

    @GetMapping("/series/campaign")
    public Result<Map<String, Object>> getCampaignProgress(@RequestParam Integer seriesId, @RequestParam String username) {
        try {
            return Result.success(gameService.getCampaignProgress(seriesId, username));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取闯关地图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/series/{id}")
    public Result<String> deleteSeries(@PathVariable("id") Integer id) {
        gameService.deleteSeriesById(id);
        return Result.success("删除成功");
    }

    // 🟢 新增：获取古建图鉴数据，供玩家游戏内随时拉取
    @GetMapping("/architectures")
    public Result<List<ScriptArchitecture>> getArchitectures(@RequestParam Integer scriptId) {
        try {
            return Result.success(gameService.getArchitecturesByScriptId(scriptId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取古建图鉴失败: " + e.getMessage());
        }
    }
}