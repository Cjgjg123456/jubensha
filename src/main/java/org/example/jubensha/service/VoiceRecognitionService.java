package org.example.jubensha.service;

import com.alibaba.fastjson2.JSONObject;
import org.example.jubensha.config.PythonVoiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 语音识别服务（支持本地 Vosk 和 Python 中间层）
 * 
 * 当启用 Python 中间层时，优先使用 Python 服务
 * 否则使用本地 Vosk 引擎
 */
@Service
public class VoiceRecognitionService {

    @Autowired(required = false)
    private PythonVoiceConfig pythonVoiceConfig;

    @Autowired(required = false)
    private PythonVoiceService pythonVoiceService;

    // 本地 Vosk 模型
    private static Model model;
    private static final String MODEL_PATH = "vosk-model-small-cn-0.22";
    
    // 单例模式确保模型只加载一次
    private static final Object lock = new Object();

    /**
     * 将音频文件转换为文字
     * 自动选择使用 Python 中间层或本地 Vosk
     */
    public String convertFileToText(String audioFilePath) {
        // 优先使用 Python 中间层
        boolean usePython = false;
        if (pythonVoiceService == null) {
            System.out.println("⚠️ pythonVoiceService 为 null，无法使用 Python 中间层");
        }
        if (pythonVoiceConfig == null) {
            System.out.println("⚠️ pythonVoiceConfig 为 null，无法使用 Python 中间层");
        }
        if (pythonVoiceConfig != null && !pythonVoiceConfig.isEnabled()) {
            System.out.println("⚠️ Python 中间层未启用 (voice.python.enabled=false)");
        }
        
        if (pythonVoiceService != null && pythonVoiceConfig != null && pythonVoiceConfig.isEnabled()) {
            usePython = true;
            System.out.println("🚀 使用 Python 中间层进行语音识别...");
            try {
                File audioFile = new File(audioFilePath);
                byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
                String fileName = audioFile.getName();
                
                String result = pythonVoiceService.recognize(audioBytes, fileName);
                
                if (result != null) {
                    System.out.println("✅ Python 中间层识别成功：" + result);
                    return result;
                } else {
                    System.out.println("⚠️ Python 中间层识别失败，回退到本地 Vosk");
                    usePython = false;
                }
            } catch (Exception e) {
                System.err.println("❌ Python 中间层调用失败：" + e.getMessage());
                System.out.println("⚠️ 回退到本地 Vosk");
                usePython = false;
            }
        }
        
        // 使用本地 Vosk
        System.out.println("🔧 使用本地 Vosk 引擎进行语音识别...");
        
        // 检查文件格式
        File audioFile = new File(audioFilePath);
        String lowerName = audioFile.getName().toLowerCase();
        boolean isWebM = lowerName.endsWith(".webm");
        
        if (isWebM && !usePython) {
            // WebM 格式需要 FFmpeg 转换
            System.out.println("⚠️ WebM 格式需要 FFmpeg 转换，尝试使用 Python 服务...");
            
            // 再次尝试直接调用 Python 服务（绕过配置检查）
            if (pythonVoiceService != null) {
                try {
                    byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
                    String fileName = audioFile.getName();
                    String result = pythonVoiceService.recognize(audioBytes, fileName);
                    if (result != null) {
                        System.out.println("✅ Python 中间层识别成功：" + result);
                        return result;
                    }
                } catch (Exception e) {
                    System.err.println("❌ 直接调用 Python 服务失败：" + e.getMessage());
                }
            }
            
            // 检查是否安装了 FFmpeg
            if (!isFfmpegAvailable()) {
                throw new RuntimeException("WebM 格式需要安装 FFmpeg 或启用 Python 中间层才能识别。请安装 FFmpeg 或确保 Python 服务正常运行。");
            }
        }
        
        return convertFileToTextLocal(audioFilePath);
    }
    
    /**
     * 检查 FFmpeg 是否可用
     */
    private boolean isFfmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("⚠️ FFmpeg 不可用：" + e.getMessage());
            return false;
        }
    }

    /**
     * 本地 Vosk 语音识别（支持多种格式）
     */
    private String convertFileToTextLocal(String audioFilePath) {
        ensureModelInitialized();
        
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("音频文件不存在：" + audioFilePath);
        }
        
        String wavFilePath = audioFilePath;
        boolean isTempFile = false;
        
        // 判断是否需要格式转换（WebM、MP4、OGG、M4A 等格式需要转换）
        String lowerName = audioFile.getName().toLowerCase();
        if (lowerName.endsWith(".webm") || lowerName.endsWith(".mp4") || 
            lowerName.endsWith(".ogg") || lowerName.endsWith(".m4a") ||
            lowerName.endsWith(".mp3") || lowerName.endsWith(".flac")) {
            try {
                wavFilePath = convertToWavWithFfmpeg(audioFilePath);
                isTempFile = true;
                System.out.println("✅ 使用 FFmpeg 转换格式：" + audioFilePath + " -> " + wavFilePath);
            } catch (Exception e) {
                System.err.println("❌ FFmpeg 转换失败：" + e.getMessage());
                System.err.println("⚠️  FFmpeg 可能未安装或路径配置不正确");
                System.err.println("⚠️  尝试直接处理原始音频文件（可能失败）");
                // 如果 FFmpeg 转换失败，尝试使用 JavaSound 直接读取（可能失败）
            }
        }
        
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(wavFilePath))) {
            
            String result = processAudioStream(audioStream);
            
            // 如果是转换后的临时文件，删除它
            if (isTempFile && !wavFilePath.equals(audioFilePath)) {
                try {
                    Files.deleteIfExists(Paths.get(wavFilePath));
                } catch (Exception e) {
                    System.err.println("⚠️ 删除临时文件失败：" + wavFilePath);
                }
            }
            
            return result;
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ Java Sound API 不支持此音频格式");
            System.err.println("📋 支持的格式：WAV, AIFF, AU");
            System.err.println("💡 提示：请安装 FFmpeg 以便支持更多音频格式");
            
            // 清理临时文件
            if (isTempFile && !wavFilePath.equals(audioFilePath)) {
                try {
                    Files.deleteIfExists(Paths.get(wavFilePath));
                } catch (Exception ex) {
                    // ignore
                }
            }
            
            throw new RuntimeException("不支持的音频格式，请确保录制为 WAV 格式或安装 FFmpeg", e);
            
        } catch (Exception e) {
            // 清理临时文件
            if (isTempFile && !wavFilePath.equals(audioFilePath)) {
                try {
                    Files.deleteIfExists(Paths.get(wavFilePath));
                } catch (Exception ex) {
                    // ignore
                }
            }
            throw new RuntimeException("处理音频文件失败：" + audioFilePath, e);
        }
    }

    /**
     * 使用 FFmpeg 将音频文件转换为 WAV 格式
     */
    private String convertToWavWithFfmpeg(String inputFilePath) throws IOException, InterruptedException {
        File inputFile = new File(inputFilePath);
        String outputFilePath = inputFilePath + "_converted.wav";
        
        // 构建 FFmpeg 命令
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", inputFilePath,
            "-acodec", "pcm_s16le",
            "-ar", "16000",
            "-ac", "1",
            "-y",
            outputFilePath
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("FFmpeg: " + line);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 转换失败，退出码：" + exitCode);
        }
        
        return outputFilePath;
    }

    /**
     * 初始化语音识别模型
     */
    public void initModel() {
        if (model != null) {
            System.out.println("Vosk 模型已加载，跳过重复初始化");
            return;
        }
        
        synchronized (lock) {
            if (model != null) {
                System.out.println("Vosk 模型已在其他线程加载，跳过重复初始化");
                return;
            }
            
            try {
                System.out.println("========================================");
                System.out.println("  Vosk 离线语音识别模型初始化");
                System.out.println("========================================");
                
                LibVosk.setLogLevel(LogLevel.WARNINGS);
                System.out.println("✓ Vosk 日志级别设置为：WARNINGS");
                
                String modelFullPath = getModelPath();
                System.out.println("📁 模型路径：" + modelFullPath);
                
                File modelDir = new File(modelFullPath);
                if (!modelDir.exists()) {
                    throw new RuntimeException("模型目录不存在：" + modelFullPath);
                }
                
                System.out.println("⏳ 正在加载模型文件...");
                long startTime = System.currentTimeMillis();
                
                model = new Model(modelFullPath);
                
                long loadTime = System.currentTimeMillis() - startTime;
                System.out.println("✅ Vosk 模型加载成功！耗时：" + loadTime + "ms");
                System.out.println("🎤 离线语音转文字功能已就绪");
                System.out.println("========================================");
                
            } catch (Exception e) {
                System.err.println("❌ Vosk 模型加载失败：" + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("无法加载语音识别模型：" + e.getMessage(), e);
            }
        }
    }

    private String getModelPath() {
        Path currentPath = Paths.get(MODEL_PATH);
        if (currentPath.toFile().exists()) {
            return currentPath.toAbsolutePath().toString();
        }
        
        Path resourcePath = Paths.get("src", "main", "resources", "static", MODEL_PATH);
        if (resourcePath.toFile().exists()) {
            return resourcePath.toAbsolutePath().toString();
        }
        
        Path targetPath = Paths.get("target", "classes", "static", MODEL_PATH);
        if (targetPath.toFile().exists()) {
            return targetPath.toAbsolutePath().toString();
        }
        
        return MODEL_PATH;
    }

    private String processAudioStream(AudioInputStream audioStream) throws IOException {
        AudioFormat sourceFormat = audioStream.getFormat();
        
        System.out.println("📊 源音频格式详情:");
        System.out.println("   - 采样率：" + sourceFormat.getSampleRate() + " Hz");
        System.out.println("   - 位深：" + sourceFormat.getSampleSizeInBits() + " bits");
        System.out.println("   - 声道数：" + sourceFormat.getChannels());

        AudioFormat targetFormat = getAudioFormat(16000);
        AudioInputStream convertedStream = audioStream;
        
        boolean needsConversion = (sourceFormat.getSampleRate() != 16000 ||
                                   sourceFormat.getSampleSizeInBits() != 16 ||
                                   sourceFormat.getChannels() != 1);
        
        if (needsConversion) {
            System.out.println("⚠️  音频格式不符合要求，尝试转换...");
            
            if (AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                System.out.println("✅ 音频转换成功");
            } else {
                System.err.println("❌ 不支持自动转换");
            }
        }

        StringBuilder resultText = new StringBuilder();

        try (Recognizer recognizer = new Recognizer(model, 16000)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = convertedStream.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = recognizer.getResult();
                    String text = extractTextFromJson(result);
                    if (!text.isEmpty()) {
                        resultText.append(text).append(" ");
                    }
                }
            }

            String finalResult = recognizer.getFinalResult();
            String text = extractTextFromJson(finalResult);
            if (!text.isEmpty()) {
                resultText.append(text);
            }
        }

        return cleanChineseSpaces(resultText.toString().trim());
    }
    
    /**
     * 清理中文之间的多余空格
     * Vosk 识别结果通常会在每个词之间加空格，需要去掉
     */
    private String cleanChineseSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // ✅ 彻底清理所有中文之间的空格
        // 方法1：使用正则表达式，去掉所有连续空格
        String result = text.replaceAll("\\s+", " ").trim();
        
        // 方法2：去掉中文字符之间的所有空格
        result = result.replaceAll("[\\u4e00-\\u9fa5\\s]+(?=[\\u4e00-\\u9fa5])", "")
                      .replaceAll("(?<=[\\u4e00-\\u9fa5])\\s+", "");
        
        // 方法3：去掉中文和英文/数字之间的空格（更彻底）
        result = result.replaceAll("[\\u4e00-\\u9fa5]\\s+", "")
                      .replaceAll("\\s+[\\u4e00-\\u9fa5]", "");
        
        // 方法4：去掉所有连续空格
        result = result.replaceAll("\\s+", "");
        
        // 最终trim
        return result.trim();
    }
    
    /**
     * 判断字符是否为中文字符
     */
    private boolean isChineseChar(char c) {
        // 基本汉字范围
        return (c >= 0x4E00 && c <= 0x9FFF) ||
               (c >= 0x3400 && c <= 0x4DBF) ||
               // 中文标点符号
               (c >= 0x3000 && c <= 0x303F) ||
               (c >= 0xFF00 && c <= 0xFFEF);
    }

    private String extractTextFromJson(String jsonResult) {
        try {
            System.out.println("📋 原始 Vosk 结果: " + truncateForLog(jsonResult));
            
            // 首先尝试直接从字节级别修复编码
            String fixedResult = fixVoskEncoding(jsonResult);
            System.out.println("📋 修复后结果: " + truncateForLog(fixedResult));
            
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"text\"\\s*:\\s*\"([^\"]*)\"");
            java.util.regex.Matcher matcher = pattern.matcher(fixedResult);
            
            if (matcher.find()) {
                String text = matcher.group(1);
                // 再次检查提取出的文本是否有编码问题
                return fixVoskEncoding(text);
            }
            
            JSONObject json = JSONObject.parseObject(fixedResult);
            String text = json.getString("text");
            return fixVoskEncoding(text);
        } catch (Exception e) {
            System.err.println("解析识别结果 JSON 失败：" + e.getMessage());
            return "";
        }
    }
    
    /**
     * 专门修复 Vosk JNI 返回的编码问题
     * Vosk 通过 JNI 返回的字符串可能被错误解码为 GBK
     */
    private String fixVoskEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 检查是否已经是有效的中文
        if (isValidChineseText(input) && !containsGarbage(input)) {
            return input;
        }
        
        // 方法1: 将字符串转为 GBK 字节，再用 UTF-8 解码（最常见的情况）
        try {
            byte[] gbkBytes = input.getBytes("GBK");
            String decoded = new String(gbkBytes, "UTF-8");
            if (isValidChineseText(decoded) && !containsGarbage(decoded)) {
                return decoded;
            }
        } catch (Exception e) {}
        
        // 方法2: 尝试 ISO-8859-1 -> UTF-8
        try {
            byte[] isoBytes = input.getBytes("ISO-8859-1");
            String decoded = new String(isoBytes, "UTF-8");
            if (isValidChineseText(decoded) && !containsGarbage(decoded)) {
                return decoded;
            }
        } catch (Exception e) {}
        
        // 方法3: 逐字符修复
        StringBuilder fixed = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // ASCII 字符直接保留
            if (c < 128) {
                fixed.append(c);
                continue;
            }
            
            // 尝试修复单个字符
            String charFixed = fixSingleChar(c);
            fixed.append(charFixed);
        }
        
        String result = fixed.toString();
        if (isValidChineseText(result) && !containsGarbage(result)) {
            return result;
        }
        
        return input;
    }
    
    /**
     * 修复单个字符的编码
     */
    private String fixSingleChar(char c) {
        // 尝试 GBK -> UTF-8
        try {
            byte[] gbkBytes = String.valueOf(c).getBytes("GBK");
            String decoded = new String(gbkBytes, "UTF-8");
            if (isValidChinese(decoded)) {
                return decoded;
            }
        } catch (Exception e) {}
        
        // 尝试 ISO-8859-1 -> UTF-8
        try {
            byte[] isoBytes = String.valueOf(c).getBytes("ISO-8859-1");
            String decoded = new String(isoBytes, "UTF-8");
            if (isValidChinese(decoded)) {
                return decoded;
            }
        } catch (Exception e) {}
        
        // 无法修复，返回原字符
        return String.valueOf(c);
    }
    
    /**
     * 检查单个字符是否为有效中文
     */
    private boolean isValidChinese(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        char c = s.charAt(0);
        return (c >= 0x4E00 && c <= 0x9FFF) ||  // 基本汉字
               (c >= 0x3400 && c <= 0x4DBF) ||  // 扩展A区
               (c >= 0x20000 && c <= 0x2A6DF);  // 扩展B区
    }
    
    /**
     * 检查字符串是否包含乱码标记
     */
    private boolean containsGarbage(String input) {
        if (input == null) return false;
        // 检查是否包含问号乱码或替换字符
        return input.contains("??") || input.contains("\uFFFD");
    }

    /**
     * 修复可能的编码问题
     * 处理 UTF-8 被错误解码为其他编码（如 GBK、ISO-8859-1）的情况
     */
    private String fixEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 首先检查是否已经是有效的中文
        if (isValidChineseText(input)) {
            return input;
        }
        
        try {
            // 0. 优先尝试 GBK -> UTF-8 转换（Vosk JNI 最常见的问题）
            String fixed = tryConvertEncoding(input, "GBK", "UTF-8");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(GBK→UTF-8): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 1. 尝试 UTF-8 -> ISO-8859-1 -> UTF-8 转换
            fixed = tryConvertEncoding(input, "ISO-8859-1", "UTF-8");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(ISO-8859-1→UTF-8): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 2. 尝试 GBK 转换
            fixed = tryConvertEncoding(input, "ISO-8859-1", "GBK");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(ISO-8859-1→GBK): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 3. 尝试 GB2312 转换
            fixed = tryConvertEncoding(input, "ISO-8859-1", "GB2312");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(ISO-8859-1→GB2312): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 4. 尝试 UTF-16BE 转换
            fixed = tryConvertEncoding(input, "ISO-8859-1", "UTF-16BE");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(ISO-8859-1→UTF-16BE): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 5. 尝试 UTF-16LE 转换
            fixed = tryConvertEncoding(input, "ISO-8859-1", "UTF-16LE");
            if (fixed != null && isValidChineseText(fixed)) {
                System.out.println("🔧 修复编码(ISO-8859-1→UTF-16LE): " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 6. 尝试 UTF-8 直接转换（处理 BOM 问题）
            fixed = removeUtf8Bom(input);
            if (!fixed.equals(input) && isValidChineseText(fixed)) {
                System.out.println("🔧 移除 UTF-8 BOM: " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 7. 尝试清理不可见字符
            fixed = cleanInvisibleCharacters(input);
            if (!fixed.equals(input) && isValidChineseText(fixed)) {
                System.out.println("🔧 清理不可见字符: " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 8. 尝试处理 Unicode 转义序列
            fixed = unescapeUnicode(input);
            if (!fixed.equals(input) && isValidChineseText(fixed)) {
                System.out.println("🔧 解析 Unicode 转义: " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
            // 9. 尝试深度修复：逐字符分析并重建
            fixed = deepFixEncoding(input);
            if (fixed != null && !fixed.equals(input) && isValidChineseText(fixed)) {
                System.out.println("🔧 深度修复编码: " + truncateForLog(input) + " -> " + truncateForLog(fixed));
                return fixed;
            }
            
        } catch (Exception e) {
            System.err.println("修复编码失败: " + e.getMessage());
        }
        
        // 如果无法修复，返回原始输入但记录日志
        if (containsUtf8Mojibake(input)) {
            System.err.println("⚠️ 无法修复乱码文本: " + truncateForLog(input));
            logStringDebugInfo(input);
        }
        
        return input;
    }
    
    /**
     * 深度修复编码：逐字符分析并尝试重建正确的 UTF-8 字符串
     */
    private String deepFixEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // 如果是正常 ASCII 字符，直接保留
            if (c < 128) {
                result.append(c);
                continue;
            }
            
            // 尝试将字符转换为 GBK 字节，然后用 UTF-8 解码
            try {
                byte[] gbkBytes = String.valueOf(c).getBytes("GBK");
                String decoded = new String(gbkBytes, "UTF-8");
                
                // 检查解码后是否为有效中文字符
                if (isValidChineseText(decoded)) {
                    result.append(decoded);
                } else {
                    // 尝试其他编码组合
                    byte[] isoBytes = String.valueOf(c).getBytes("ISO-8859-1");
                    decoded = new String(isoBytes, "UTF-8");
                    if (isValidChineseText(decoded)) {
                        result.append(decoded);
                    } else {
                        result.append(c);
                    }
                }
            } catch (Exception e) {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 尝试编码转换
     */
    private String tryConvertEncoding(String input, String sourceEncoding, String targetEncoding) {
        try {
            byte[] bytes = input.getBytes(sourceEncoding);
            return new String(bytes, targetEncoding);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 移除 UTF-8 BOM 标记
     */
    private String removeUtf8Bom(String input) {
        if (input != null && input.startsWith("\uFEFF")) {
            return input.substring(1);
        }
        return input;
    }
    
    /**
     * 清理不可见字符
     */
    private String cleanInvisibleCharacters(String input) {
        if (input == null) {
            return null;
        }
        // 移除控制字符，但保留必要的空白字符
        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }
    
    /**
     * 解析 Unicode 转义序列
     */
    private String unescapeUnicode(String input) {
        if (input == null) {
            return null;
        }
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\\\u([0-9a-fA-F]{4})");
            java.util.regex.Matcher matcher = pattern.matcher(input);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                char c = (char) Integer.parseInt(matcher.group(1), 16);
                matcher.appendReplacement(sb, Character.toString(c));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
    
    /**
     * 截断字符串用于日志输出
     */
    private String truncateForLog(String input) {
        if (input == null) {
            return "null";
        }
        return input.length() > 20 ? input.substring(0, 20) + "..." : input;
    }
    
    /**
     * 输出字符串的调试信息
     */
    private void logStringDebugInfo(String input) {
        if (input == null) {
            return;
        }
        System.out.println("📊 字符串调试信息:");
        System.out.println("   长度: " + input.length());
        System.out.println("   原始字符: " + input);
        try {
            byte[] utf8Bytes = input.getBytes("UTF-8");
            System.out.println("   UTF-8 字节: " + bytesToHex(utf8Bytes));
            byte[] isoBytes = input.getBytes("ISO-8859-1");
            System.out.println("   ISO-8859-1 字节: " + bytesToHex(isoBytes));
        } catch (Exception e) {
            System.err.println("   编码转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    
    /**
     * 验证是否为有效的中文文本
     */
    private boolean isValidChineseText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        int chineseCount = 0;
        int totalCount = 0;
        int validCharCount = 0;
        
        for (char c : text.toCharArray()) {
            totalCount++;
            
            // 中文字符范围
            if ((c >= 0x4E00 && c <= 0x9FFF) ||      // 基本汉字
                (c >= 0x3400 && c <= 0x4DBF) ||      // 扩展A区
                (c >= 0x20000 && c <= 0x2A6DF)) {    // 扩展B区
                chineseCount++;
                validCharCount++;
            }
            // 常见标点符号
            else if ((c >= 0x3000 && c <= 0x303F) || // CJK标点
                     (c >= 0xFF00 && c <= 0xFFEF) || // 全角ASCII
                     (c >= 0x2000 && c <= 0x206F)) { // 通用标点
                validCharCount++;
            }
            // 基本ASCII字符
            else if (c >= 0x20 && c <= 0x7E) {
                validCharCount++;
            }
        }
        
        // 如果文本中大部分是有效字符，且包含一定比例的中文，则认为是有效中文
        boolean hasEnoughValidChars = validCharCount >= totalCount * 0.8;
        boolean hasEnoughChinese = chineseCount >= 2 || (chineseCount >= 1 && totalCount <= 5);
        
        return hasEnoughValidChars && hasEnoughChinese;
    }

    /**
     * 检测字符串是否包含 UTF-8 乱码（Mojibake）
     * 典型特征：包含多个连续的乱码字符
     */
    private boolean containsUtf8Mojibake(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        // 扩展的乱码模式列表
        String[] mojibakePatterns = {
            // UTF-8被错误解码为GBK的典型乱码
            "鎮", "ㄥ", "ソ", "娆", "㈣", "繋", "杩", "涘", "叆",
            "娓", "告", "垙", "璇", "寮", "€", "濮", "閫", "夋",
            "嫨", "浣", "鐨", "瑙", "掕", "壊", "鍙", "栫", "紨",
            "榛", "闆", "鍦", "堝", "濞", "椋", "涔", "鎬", "椁",
            "蹇", "鍘", "氱", "噯", "澶", "犺", "浠", "銆", "绂",
            "鐩", "鏄", "鎴", "戝", "浠", "鐨", "栧", "鏂", "鎵",
            // UTF-8被错误解码为ISO-8859-1的典型乱码
            "Ã", "Â", "Ä", "É", "Ê", "Ë", "Í", "Ï", "Ó", "Ô",
            "Ö", "Ú", "Ü", "ß", "à", "â", "ä", "é", "è", "ê",
            "ë", "í", "ì", "ï", "ó", "ô", "ö", "ú", "ù", "ü",
            // 其他常见乱码字符
            "ï¿½", "ð", "ñ", "Ñ", "ð", "þ", "ÿ", "ª", "º", "¿"
        };

        int matchCount = 0;
        for (String pattern : mojibakePatterns) {
            if (input.contains(pattern)) {
                matchCount++;
            }
        }
        
        // 匹配到2个以上乱码模式即认为存在乱码
        if (matchCount >= 2) {
            return true;
        }
        
        // 检测可疑字符范围（可能是乱码的特征）
        char[] chars = input.toCharArray();
        int suspiciousCount = 0;
        
        for (char c : chars) {
            // CJK兼容区和扩展区的可疑字符
            if ((c >= 0xF900 && c <= 0xFAFF) ||      // CJK兼容表意符号
                (c >= 0xFE30 && c <= 0xFE4F) ||      // CJK兼容形式
                (c >= 0xFF65 && c <= 0xFFDC)) {      // 半角假名
                suspiciousCount++;
            }
            // 高Unicode区域的可疑字符
            else if (c >= 0xE000 && c <= 0xF8FF) {   // 私人使用区
                suspiciousCount++;
            }
        }
        
        // 如果可疑字符数量超过一定比例，认为存在乱码
        if (suspiciousCount >= 2 && suspiciousCount < chars.length * 0.5) {
            return true;
        }
        
        // 检测连续的非ASCII非中文符号
        int consecutiveNonChinese = 0;
        for (char c : chars) {
            boolean isChinese = (c >= 0x4E00 && c <= 0x9FFF);
            boolean isAscii = (c >= 0x20 && c <= 0x7E);
            
            if (!isChinese && !isAscii) {
                consecutiveNonChinese++;
                if (consecutiveNonChinese >= 4) {
                    return true;
                }
            } else {
                consecutiveNonChinese = 0;
            }
        }
        
        return false;
    }

    private AudioFormat getAudioFormat(int sampleRate) {
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            16,
            1,
            2,
            sampleRate,
            false
        );
    }

    private void ensureModelInitialized() {
        if (model == null) {
            initModel();
        }
    }

    public boolean isModelLoaded() {
        return model != null;
    }
}