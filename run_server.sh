#!/bin/bash
echo "启动银行系统服务器..."
echo "使用说明:"
echo "  ./run_server.sh              # 启动 At-Most-Once 模式"
echo "  ./run_server.sh ALO          # 启动 At-Least-Once 模式"
echo ""

cd "$(dirname "$0")"

if [ "$1" == "ALO" ]; then
    echo "启动 At-Least-Once 模式服务器..."
    java -cp bin Server.ServerMain ALO
else
    echo "启动 At-Most-Once 模式服务器..."
    java -cp bin Server.ServerMain
fi