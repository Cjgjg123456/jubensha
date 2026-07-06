package org.example.jubensha.config;

import com.sun.jna.Native;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Vosk 原生库配置
 * 负责加载和管理 Vosk 所需的 .dll/.so 动态链接库
 */
@Configuration
public class VoskLibraryConfig {

    private static final String LIBRARY_NAME = "vosk";
    private static final String TEMP_LIB_DIR = "jubensha_libs";

    /**
     * 应用启动时自动加载原生库
     */
    @PostConstruct
    public void loadNativeLibrary() {
        System.out.println("=== 开始配置 Vosk 原生库 ===");
        
        try {
            // 方法1: 尝试从当前工作目录加载
            String osName = System.getProperty("os.name").toLowerCase();
            String libFileName = osName.contains("win") ? "libvosk.dll" : 
                                osName.contains("mac") ? "libvosk.dylib" : "libvosk.so";
            
            Path currentDirLib = Paths.get(libFileName);
            if (Files.exists(currentDirLib)) {
                System.load(currentDirLib.toAbsolutePath().toString());
                System.out.println("✓ Vosk 原生库从当前目录加载成功: " + currentDirLib.toAbsolutePath());
                return;
            }
            
            // 方法2: 尝试直接加载（如果系统已安装）
            try {
                System.loadLibrary(LIBRARY_NAME);
                System.out.println("✓ Vosk 原生库加载成功（系统路径）");
                return;
            } catch (UnsatisfiedLinkError e) {
                System.out.println("系统路径未找到 Vosk 库，尝试从资源加载...");
            }
            
            // 方法3: 从 classpath 提取并加载
            extractAndLoadLibrary();
            
        } catch (Exception e) {
            System.err.println("⚠ Vosk 原生库加载失败: " + e.getMessage());
            System.err.println("提示：语音识别功能可能无法使用");
            System.err.println("请确保已将 vosk.dll (Windows) 或 libvosk.so (Linux) 放置在正确位置");
            e.printStackTrace();
        }
    }

    /**
     * 从资源文件提取并加载原生库
     */
    private void extractAndLoadLibrary() throws IOException {
        // 确定平台特定的库文件名
        String osName = System.getProperty("os.name").toLowerCase();
        String libFileName;
        
        if (osName.contains("win")) {
            // Windows 下尝试多个可能的文件名
            String[] possibleNames = {"libvosk.dll", "vosk.dll"};
            libFileName = null;
            
            for (String name : possibleNames) {
                String resourcePath = "/native/" + name;
                if (getClass().getResourceAsStream(resourcePath) != null) {
                    libFileName = name;
                    break;
                }
            }
            
            if (libFileName == null) {
                libFileName = "libvosk.dll"; // 默认使用 libvosk.dll
            }
        } else if (osName.contains("mac")) {
            libFileName = "libvosk.dylib";
        } else {
            libFileName = "libvosk.so";
        }
        
        System.out.println("检测到操作系统: " + osName);
        System.out.println("需要加载的库文件: " + libFileName);
        
        // 创建临时目录
        Path tempDir = Paths.get(TEMP_LIB_DIR);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        
        // 复制所有 native DLL 文件到临时目录（确保依赖项可用）
        String nativeResourcePath = "/native/";
        java.util.List<String> dllFiles = new java.util.ArrayList<>();
        
        // 列出所有可能的 DLL 文件
        String[] allDlls = {"libvosk.dll", "vosk.dll", "libgcc_s_seh-1.dll", 
                           "libstdc++-6.dll", "libwinpthread-1.dll"};
        
        for (String dllName : allDlls) {
            InputStream dllStream = getClass().getResourceAsStream(nativeResourcePath + dllName);
            if (dllStream != null) {
                Path dllPath = tempDir.resolve(dllName);
                // 检查文件是否已存在，避免 Windows 上的 FileAlreadyExistsException
                if (!Files.exists(dllPath)) {
                    Files.copy(dllStream, dllPath);
                } else {
                    System.out.println("  文件已存在，跳过复制: " + dllName);
                }
                dllStream.close();
                dllFiles.add(dllName);
                System.out.println("✓ 已提取: " + dllName);
            }
        }
        
        if (dllFiles.isEmpty()) {
            System.out.println("⚠ 未在资源中找到任何原生库文件");
            System.out.println("请手动将 DLL 文件放置到以下位置之一：");
            System.out.println("  1. 系统 PATH 环境变量指定的目录");
            System.out.println("  2. java.library.path 指定的目录");
            System.out.println("  3. src/main/resources/native/ 目录");
            System.out.println("当前 java.library.path: " + System.getProperty("java.library.path"));
            return;
        }
        
        // 将临时目录添加到 java.library.path
        try {
            String tempDirPath = tempDir.toAbsolutePath().toString();
            System.setProperty("java.library.path", 
                System.getProperty("java.library.path") + ";" + tempDirPath);
            
            // 清除 ClassLoader 缓存
            java.lang.reflect.Field fieldSysPath = java.lang.ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
            
            System.out.println("✓ 已将临时目录添加到 library path: " + tempDirPath);
        } catch (Exception e) {
            System.err.println("⚠ 无法更新 java.library.path: " + e.getMessage());
        }
        
        // 加载主库文件
        Path mainLibPath = tempDir.resolve(libFileName);
        if (Files.exists(mainLibPath)) {
            try {
                System.load(mainLibPath.toAbsolutePath().toString());
                System.out.println("✓ Vosk 原生库从资源加载成功: " + mainLibPath.toAbsolutePath());
                System.out.println("✓ 已加载的依赖库: " + String.join(", ", dllFiles));
            } catch (UnsatisfiedLinkError e) {
                System.err.println("⚠ 加载主库失败，可能是架构不匹配或依赖缺失");
                System.err.println("  错误详情: " + e.getMessage());
                System.err.println("  建议：将 DLL 文件所在目录添加到系统 PATH 环境变量");
            }
        } else {
            System.err.println("⚠ 主库文件不存在: " + libFileName);
        }
    }

    /**
     * 获取原生库目录路径
     */
    public static String getNativeLibDir() {
        return new File(TEMP_LIB_DIR).getAbsolutePath();
    }
}
