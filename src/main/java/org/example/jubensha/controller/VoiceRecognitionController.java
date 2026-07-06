package org.example.jubensha.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.example.jubensha.common.Result;
import org.example.jubensha.service.VoiceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音识别控制器
 * 提供离线语音转文字的 REST API 接口
 */
@RestController
@RequestMapping("/api/voice")
public class VoiceRecognitionController {

    private final VoiceRecognitionService voiceRecognitionService;

    @Autowired
    public VoiceRecognitionController(@Lazy VoiceRecognitionService voiceRecognitionService) {
        this.voiceRecognitionService = voiceRecognitionService;
    }

    // 使用绝对路径，确保临时文件保存在项目根目录
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "jubensha_uploads" + File.separator + "voice_temp" + File.separator;

    /**
     * 上传音频文件并转换为文字
     * 
     * @param file 音频文件（WAV/WebM/MP4 格式）
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public ResponseEntity<Result<Map<String, Object>>> recognizeAudio(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Result.fail("上传的文件为空"));
            }
            
            // 检查文件大小（最大 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Result.fail("文件大小超过限制（最大 10MB）"));
            }
            
            String originalFilename = file.getOriginalFilename();
            System.out.println("🎤 收到语音识别请求: " + originalFilename + " (" + file.getSize() + " bytes)");
            
            // 支持多种音频格式
            if (originalFilename == null) {
                return ResponseEntity.badRequest()
                    .body(Result.fail("文件名无效"));
            }
            
            String lowerName = originalFilename.toLowerCase();
            boolean isSupported = lowerName.endsWith(".wav") || 
                                 lowerName.endsWith(".webm") || 
                                 lowerName.endsWith(".mp4") ||
                                 lowerName.endsWith(".ogg") ||
                                 lowerName.endsWith(".m4a") ||
                                 lowerName.endsWith(".mp3") ||
                                 lowerName.endsWith(".flac");
            
            if (!isSupported) {
                return ResponseEntity.badRequest()
                    .body(Result.fail("不支持的音频格式，请使用 WAV/M4A/MP3/FLAC/WebM/MP4/OGG 格式"));
            }
            
            // 保存临时文件
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ 创建语音上传目录: " + uploadPath.toAbsolutePath());
            }
            
            String tempFileName = UUID.randomUUID().toString() + getFileExtension(originalFilename);
            Path tempFilePath = uploadPath.resolve(tempFileName);
            file.transferTo(tempFilePath.toFile());
            System.out.println("📁 临时文件已保存: " + tempFilePath);
            
            // 执行语音识别
            System.out.println("⏳ 开始语音识别...");
            long startTime = System.currentTimeMillis();
            
            String recognizedText = voiceRecognitionService.convertFileToText(
                tempFilePath.toString()
            );
            
            long processingTime = System.currentTimeMillis() - startTime;
            System.out.println("✅ 识别完成，耗时: " + processingTime + "ms");
            
            // 调试：打印原始识别结果的字节表示
            if (recognizedText != null && !recognizedText.isEmpty()) {
                System.out.println("📝 识别结果长度: " + recognizedText.length());
                System.out.println("📝 识别结果字符: " + recognizedText);
                try {
                    byte[] utf8Bytes = recognizedText.getBytes("UTF-8");
                    System.out.println("📝 UTF-8 字节长度: " + utf8Bytes.length);
                } catch (Exception e) {
                    System.err.println("编码转换失败: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ 识别结果为空");
            }
            
            // 构建响应
            response.put("text", recognizedText);
            response.put("processingTime", processingTime + "ms");
            response.put("fileName", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("success", true);
            
            // 删除临时文件
            Files.deleteIfExists(tempFilePath);
            System.out.println("🗑️  临时文件已删除");
            
            // 设置响应头，确保 UTF-8 编码
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            return ResponseEntity.ok().headers(headers).body(Result.success(response));
            
        } catch (IOException e) {
            System.err.println("❌ 文件处理失败: " + e.getMessage());
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            return ResponseEntity.internalServerError()
                .headers(headers)
                .body(Result.fail("文件处理失败: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("❌ 语音识别失败: " + e.getMessage());
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            return ResponseEntity.internalServerError()
                .headers(headers)
                .body(Result.fail("语音识别失败: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ 未知错误: " + e.getMessage());
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            return ResponseEntity.internalServerError()
                .headers(headers)
                .body(Result.fail("服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return ".wav"; // 默认扩展名
    }

    /**
     * 检查语音识别服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Result<Map<String, Object>>> checkStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            boolean modelLoaded = voiceRecognitionService != null && voiceRecognitionService.isModelLoaded();
            status.put("modelLoaded", modelLoaded);
            status.put("serviceAvailable", true);

            return ResponseEntity.ok(Result.success(status));
        } catch (Exception e) {
            System.err.println("❌ 检查语音识别状态失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Result.fail("获取语音识别状态失败: " + e.getMessage()));
        }
    }

    /**
     * 初始化语音识别模型（可选，首次调用会自动初始化）
     */
    @PostMapping("/init")
    public ResponseEntity<Result<String>> initModel() {
        try {
            voiceRecognitionService.initModel();
            return ResponseEntity.ok(Result.success("语音识别模型初始化成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Result.fail("模型初始化失败: " + e.getMessage()));
        }
    }

}
