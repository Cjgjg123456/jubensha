@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo [INFO] Loading .env environment variables...
for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    if not "%%a"=="" set "%%a=%%b"
)

echo [INFO] Starting JubenSha server (mvn spring-boot:run)...
echo [INFO] Log: %CD%\server.log
call mvn spring-boot:run -o >> server.log 2>&1
