@echo off
chcp 65001 >nul
echo ========================================
echo   Starting JubenSha Game...
echo ========================================
echo.
cd /d "%~dp0"
java -jar target\jubensha-0.0.1-SNAPSHOT.jar
pause
