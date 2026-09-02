#!/bin/bash
# 补丁：把 BufferGeometryUtils.js 打进 jar 并重启
set -e
cd /root/jubensha-11

echo "STEP extract"
rm -rf /tmp/bu-patch && mkdir -p /tmp/bu-patch
tar xzf bufferutils.tar.gz -C /tmp/bu-patch

echo "STEP jaruf"
cd /tmp/bu-patch
jar uf /root/jubensha-11/jubensha-0.0.1-SNAPSHOT.jar \
  BOOT-INF/classes/static/vendor/three/utils/BufferGeometryUtils.js

echo "STEP verify"
jar tf /root/jubensha-11/jubensha-0.0.1-SNAPSHOT.jar | grep 'BufferGeometryUtils'

echo "STEP kill"
pkill -f 'java -jar jubensha-0.0.1-SNAPSHOT.jar' || true
sleep 3

echo "STEP start"
nohup ./start.sh > app.log 2>&1 &
echo "STEP done"
