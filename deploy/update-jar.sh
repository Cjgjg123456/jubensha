#!/bin/bash
# 增量更新 jar：把本地 three.js 与 play.html 打进已部署的 fat jar
set -e
cd /root/jubensha-11

echo "STEP backup"
cp -f jubensha-0.0.1-SNAPSHOT.jar jubensha-0.0.1-SNAPSHOT.jar.pre-three

echo "STEP extract"
rm -rf /tmp/jar-update && mkdir -p /tmp/jar-update
tar xzf jar-update.tar.gz -C /tmp/jar-update

echo "STEP jaruf"
cd /tmp/jar-update
jar uf /root/jubensha-11/jubensha-0.0.1-SNAPSHOT.jar \
  BOOT-INF/classes/static/vendor \
  BOOT-INF/classes/static/play.html

echo "STEP verify"
jar tf /root/jubensha-11/jubensha-0.0.1-SNAPSHOT.jar | grep 'vendor/three' | head -20
echo "STEP done"
