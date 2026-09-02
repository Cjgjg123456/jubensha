# 剧本杀系统 · 腾讯云 Lighthouse 部署指南

## 一、前提条件（需要你准备的东西）

| 项目 | 说明 |
|------|------|
| 腾讯云 Lighthouse 服务器 | 推荐配置：2核4G 及以上，系统选 **Ubuntu 22.04** |
| 公网 IP | Lighthouse 控制台里能查到 |
| 已备案域名 | 用于 HTTPS 访问 |
| 本地编译好的 jar | `target/jubensha-0.0.1-SNAPSHOT.jar`（本仓库已编译） |

> 注意：本次部署**先不上语音识别**（语音转文字），只保留 **TTS 语音合成**（DM 朗读台词）。

---

## 二、本地已完成的改动

1. ✅ 关闭本地语音识别：`application.properties` 中 `voice.python.enabled=false`
2. ✅ 修复 `java -jar` 启动：移除未使用的 `mysql-connector-j` 依赖
3. ✅ 默认 TTS 音色改为 `zh_male_shaonianzixin_moon_bigtts`
4. ✅ 已编译出 jar 并冒烟测试通过

---

## 三、上传文件到服务器

把下面 4 类文件上传到服务器的 `/opt/jubensha/`（用 `scp` 或 Lighthouse 网页文件管理器）：

```
jubensha-0.0.1-SNAPSHOT.jar      ← target/ 下编译好的 jar
.env                             ← 从本机 .env 复制，填入真实密钥
jubensha/example_db.sqlite       ← 数据库（不传则自动初始化新库）
uploads/                         ← 头像/图片等上传文件（可选）
```

用 scp 的示例（在本机执行）：

```bash
scp "target/jubensha-0.0.1-SNAPSHOT.jar" ubuntu@150.158.11.253:/opt/jubensha/
scp .env ubuntu@150.158.11.253:/opt/jubensha/
scp -r jubensha ubuntu@150.158.11.253:/opt/jubensha/
scp -r uploads ubuntu@150.158.11.253:/opt/jubensha/
```

---

## 四、服务器环境部署

登录服务器后，上传本目录的 `deploy.sh`、`jubensha.service`、`nginx-jubensha.conf`，然后：

```bash
sudo bash deploy.sh
```

该脚本会：安装 OpenJDK 17 + Nginx → 建目录 → 装 systemd 服务 → 装 Nginx 配置。

---

## 五、配置 Nginx（填域名）

配置已预填 `server_name ylxq.art;`。如需修改，编辑 `/etc/nginx/conf.d/jubensha.conf`：

```bash
sudo nano /etc/nginx/conf.d/jubensha.conf
sudo nginx -t
sudo systemctl reload nginx
```

---

## 六、启动服务

```bash
sudo systemctl start jubensha
sudo systemctl status jubensha
journalctl -u jubensha -f        # 实时看日志
```

看到 `Started JubenshaApplication` 即启动成功。

---

## 七、域名解析 + HTTPS

1. 到 ylxq.art 的 DNS 控制台，加一条 **A 记录**：`@` → `150.158.11.253`
2. Lighthouse 防火墙放行 **80 / 443** 端口（网页用）
3. 用 certbot 自动申请 HTTPS 证书：

```bash
sudo apt-get install -y certbot python3-certbot-nginx
sudo certbot --nginx -d ylxq.art
```

certbot 会自动给 Nginx 加 HTTPS 并续期。

---

## 八、验证

- 浏览器打开 `https://你的域名/` → 能看到登录/大厅页面
- 注册登录 → 创建/加入房间 → 进入游戏 → DM 朗读台词（TTS）正常
- 两台设备分别登录，联机聊天/游戏同步正常（走 WebSocket）
