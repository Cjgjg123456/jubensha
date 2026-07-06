@echo off
echo ========================================
echo   Cpolar 服务重启脚本
echo ========================================
echo.
echo 正在停止 cpolar 服务...
net stop cpolar

echo.
echo 等待服务停止...
timeout /t 3 /nobreak > nul

echo.
echo 正在启动 cpolar 服务...
net start cpolar

echo.
echo 等待服务启动...
timeout /t 5 /nobreak > nul

echo.
echo ========================================
echo   服务重启完成！
echo ========================================
echo.
echo 请打开浏览器访问: http://localhost:9200
echo 查看新的公网地址
echo.
pause
