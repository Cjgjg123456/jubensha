#!/usr/bin/env bash
# 剧本杀游戏 - 服务器一键环境部署脚本（Ubuntu 22.04 / Debian）
# 用法：把本脚本与 jubensha.service、nginx-jubensha.conf 一起上传到服务器，然后执行：
#   sudo bash deploy.sh
set -euo pipefail

APP_DIR=/opt/jubensha
JAR_NAME=jubensha-0.0.1-SNAPSHOT.jar

echo "==> [1/5] 更新软件源并安装 OpenJDK 17 + Nginx"
apt-get update -y
apt-get install -y openjdk-17-jdk-headless nginx

echo "==> [2/5] 创建应用目录"
mkdir -p "$APP_DIR/jubensha" "$APP_DIR/uploads"
chown -R ubuntu:ubuntu "$APP_DIR"

echo "==> [3/5] 安装 systemd 服务"
cp jubensha.service /etc/systemd/system/jubensha.service
systemctl daemon-reload
systemctl enable jubensha

echo "==> [4/5] 安装 Nginx 配置"
cp nginx-jubensha.conf /etc/nginx/conf.d/jubensha.conf
nginx -t
systemctl reload nginx

echo "==> [5/5] 手动上传以下文件到 $APP_DIR ："
echo "    - $JAR_NAME                    （编译好的 jar）"
echo "    - .env                          （密钥配置，从本机 .env 复制真实值）"
echo "    - jubensha/example_db.sqlite    （数据库，可选：不传则自动初始化新库）"
echo ""
echo "上传完成后启动服务："
echo "    sudo systemctl start jubensha"
echo "    sudo systemctl status jubensha"
echo "    journalctl -u jubensha -f"
echo ""
echo "环境部署完成。"
