package org.example.jubensha.service;

import org.example.jubensha.common.Result;
import org.example.jubensha.entity.*;

import java.util.List;
import java.util.Map;

public interface GameService {
    // ================= 核心补全：系列与闯关地图接口 =================
    // 引入 ScriptSeries 实体（如果没有import请注意导包）
    //

    List<ScriptSeries> getAllSeries();

    ScriptSeries createSeries(Map<String, Object> payload);

    Map<String, Object> getSeriesDetail(Integer seriesId);

    Map<String, Object> getCampaignProgress(Integer seriesId, String username);

    List<Script> getScriptList();
    List<Script> getScriptsByUserId(Long userId);
    List<Role> getRolesByScriptId(Integer scriptId);
    List<Act> getActsByScriptId(Integer scriptId);
    Map<String, Object> getScriptFullDetail(Integer id);

    Integer createNewScript(Map<String, Object> scriptData);
    Integer updateExistingScript(Map<String, Object> scriptData);
    void deleteScriptById(Integer scriptId, String username);

    GameProgress startGame(Long userId, Integer scriptId, Integer roleId);
    GameProgress startGameForOnline(Long userId, Integer scriptId, Integer roleId);
    Map<String, Object> getActContentWithCheck(Integer gameId, Integer targetActId);
    List<ScriptClue> getUnlockedClues(Integer gameId);
    GameProgress nextAct(Integer gameId);
    Map<String, Object> submitVote(Integer gameId, Integer votedRoleId);
    void restartGame(Integer g);
    GameProgress nextPhase(Integer g);

    List<Map<String, String>> getAiResponses(Integer gameId);
    Map<String, Object> handleAiChat(Integer gameId, List<Map<String, String>> history, String targetRoleName);

    Map<String, Object> getDmOpening(Integer gameId);
    Map<String, Object> getDmHelp(Integer gameId);

    void recordBrowseHistory(Long userId, Integer scriptId);
    Result<String> submitEvaluation(Map<String, Object> payload);
    List<Map<String, Object>> getEvaluationsByScriptId(Integer scriptId);
    int countEvaluations(Integer scriptId);
    Double getAverageScore(Integer scriptId);
    Result<String> toggleFavorite(Map<String, Object> payload);
    List<Integer> getUserFavorites(String username);

    Map<String, Object> investigateScene(Integer gameId, String actionText);
    List<String> getSceneImages(Integer gameId);

    // ================= 搜证动作配置 =================
    List<String> getAllInvestigateActions();

    List<String> getInvestigateActionsByGameId(Integer gameId);

    // ================= 新增：现场搜证卡关时的 DM 提示 =================
    Map<String, Object> getInvestigateHint(Integer gameId);

    ScriptSeries updateSeries(Map<String, Object> payload);
    ScriptSeries getSeriesById(Integer seriesId);
    void deleteSeriesById(Integer seriesId);

    List<ScriptArchitecture> getArchitecturesByScriptId(Integer scriptId);
}