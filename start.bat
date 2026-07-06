@echo off
chcp 65001 >nul
title JubenSha Game Launcher
color 0A

echo ========================================
echo    JubenSha Game - Launcher
echo ========================================
echo.

if not exist "pom.xml" (
    echo [ERROR] Please run this script in project root!
    echo Current directory: %CD%
    pause
    exit /b 1
)

if not exist "target\jubensha-0.0.1-SNAPSHOT.jar" (
    echo [WARNING] JAR file not found, compiling...
    echo.
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Compile failed!
        pause
        exit /b 1
    )
    echo.
)

echo [INFO] Checking Java environment...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found! Please install Java 17 or higher
    pause
    exit /b 1
)
echo [OK] Java environment OK
echo.

echo ========================================
echo    Select startup mode
echo ========================================
echo.
echo [1] Local Mode (LAN only)
echo [2] Public Mode (Internet access)
echo [3] Show public addresses
echo [4] Restart Cpolar
echo [5] Exit
echo.
set /p choice="Enter choice (1-5): "

if "%choice%"=="1" goto local
if "%choice%"=="2" goto public
if "%choice%"=="3" goto check
if "%choice%"=="4" goto restart_cpolar
if "%choice%"=="5" goto end

echo [ERROR] Invalid choice!
pause
goto start

:local
echo.
echo ========================================
echo    Starting Local Mode
echo ========================================
echo.
echo [INFO] Local access addresses:
echo    Web: http://localhost:8080
echo    Game Server: localhost:8888
echo.
echo [TIP] Press Ctrl+C to stop server
echo.
pause

cd /d "%~dp0"
java -jar target\jubensha-0.0.1-SNAPSHOT.jar
goto end

:public
echo.
echo ========================================
echo    Starting Public Mode
echo ========================================
echo.
echo [INFO] Current public addresses:
echo    Web: http://5ff7cc2.r11.cpolar.top
echo    Game Server: tcp://11.tcp.cpolar.top:14220
echo.
echo [TIP] Make sure Cpolar is running!
echo       Check status at: http://localhost:9200
echo.
echo [TIP] Press Ctrl+C to stop server
echo.
pause

cd /d "%~dp0"
java -jar target\jubensha-0.0.1-SNAPSHOT.jar
goto end

:check
echo.
echo ========================================
echo    Current Public Addresses
echo ========================================
echo.
echo [Web Interface]
echo   HTTP: http://5ff7cc2.r11.cpolar.top
echo   HTTPS: https://5ff7cc2.r11.cpolar.top
echo.
echo [Game Server]
echo   TCP: tcp://11.tcp.cpolar.top:14220
echo.
echo [Local Access]
echo   Web: http://localhost:8080
echo   Game: localhost:8888
echo.
echo [Cpolar Management]
echo   Dashboard: http://localhost:9200
echo.
pause
goto start

:restart_cpolar
echo.
echo [TIP] Please right-click restart-cpolar.bat and select "Run as administrator"
echo.
pause
goto start

:end
echo.
echo [DONE] Program exited
pause
