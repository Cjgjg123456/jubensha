#!/bin/bash
# 重启应用，加载更新后的 jar
cd /root/jubensha-11

echo "STEP kill"
pkill -f 'java -jar jubensha-0.0.1-SNAPSHOT.jar' || true
sleep 3

echo "STEP start"
nohup ./start.sh > app.log 2>&1 &
echo "STEP launched pid=$!"
echo "STEP done"
