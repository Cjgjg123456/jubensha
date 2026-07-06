$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   剧本杀游戏 - 启动器" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否在正确目录
if (-not (Test-Path "pom.xml")) {
    Write-Host "[错误] 请在项目根目录运行此脚本！" -ForegroundColor Red
    Write-Host "当前目录: $PWD" -ForegroundColor Yellow
    Read-Host "按回车退出"
    exit 1
}

# 检查jar文件
$jarPath = "target\jubensha-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[警告] 未找到编译文件，开始编译..." -ForegroundColor Yellow
    Write-Host ""
    try {
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[错误] 编译失败！" -ForegroundColor Red
            Read-Host "按回车退出"
            exit 1
        }
    } catch {
        Write-Host "[错误] 编译失败: $_" -ForegroundColor Red
        Read-Host "按回车退出"
        exit 1
    }
    Write-Host ""
}

# 检查Java
try {
    java -version | Out-Null
    Write-Host "[成功] Java环境正常" -ForegroundColor Green
} catch {
    Write-Host "[错误] 未找到Java！请安装Java 17或更高版本" -ForegroundColor Red
    Read-Host "按回车退出"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   请选择启动模式" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[1] 本地模式（仅局域网访问）" -ForegroundColor White
Write-Host "[2] 公网联机模式" -ForegroundColor White
Write-Host "[3] 查看当前公网地址" -ForegroundColor White
Write-Host "[4] 退出" -ForegroundColor White
Write-Host ""

$choice = Read-Host "请输入选项 (1-4)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "   启动本地模式" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "[信息] 本地访问地址:" -ForegroundColor Yellow
        Write-Host "   Web界面: http://localhost:8080" -ForegroundColor White
        Write-Host "   游戏服务器: localhost:8888" -ForegroundColor White
        Write-Host ""
        Write-Host "[提示] 按 Ctrl+C 可以停止服务器" -ForegroundColor Gray
        Write-Host ""
        Read-Host "按回车开始"
        
        java -jar $jarPath
    }
    
    "2" {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "   启动公网联机模式" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "[信息] 当前公网地址配置:" -ForegroundColor Yellow
        Write-Host "   Web界面: http://5ff7cc2.r11.cpolar.top" -ForegroundColor White
        Write-Host "   游戏服务器: tcp://11.tcp.cpolar.top:14220" -ForegroundColor White
        Write-Host ""
        Write-Host "[提示] 请确保cpolar正在运行！" -ForegroundColor Yellow
        Write-Host "       如需查看cpolar状态，访问: http://localhost:9200" -ForegroundColor Gray
        Write-Host ""
        Write-Host "[提示] 按 Ctrl+C 可以停止服务器" -ForegroundColor Gray
        Write-Host ""
        Read-Host "按回车开始"
        
        java -jar $jarPath
    }
    
    "3" {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "   当前公网地址" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "[Web界面]" -ForegroundColor Yellow
        Write-Host "  HTTP: http://5ff7cc2.r11.cpolar.top" -ForegroundColor White
        Write-Host "  HTTPS: https://5ff7cc2.r11.cpolar.top" -ForegroundColor White
        Write-Host ""
        Write-Host "[游戏服务器]" -ForegroundColor Yellow
        Write-Host "  TCP: tcp://11.tcp.cpolar.top:14220" -ForegroundColor White
        Write-Host ""
        Write-Host "[本地访问]" -ForegroundColor Yellow
        Write-Host "  Web: http://localhost:8080" -ForegroundColor White
        Write-Host "  游戏: localhost:8888" -ForegroundColor White
        Write-Host ""
        Write-Host "[Cpolar管理]" -ForegroundColor Yellow
        Write-Host "  管理界面: http://localhost:9200" -ForegroundColor White
        Write-Host ""
        Read-Host "按回车返回"
        & $PSCommandPath
    }
    
    "4" {
        Write-Host ""
        Write-Host "[完成] 程序已退出" -ForegroundColor Green
    }
    
    default {
        Write-Host "[错误] 无效选择！" -ForegroundColor Red
        Read-Host "按回车重试"
        & $PSCommandPath
    }
}
