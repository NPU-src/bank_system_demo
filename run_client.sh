#!/bin/bash
echo "启动银行系统客户端..."
echo ""

cd "$(dirname "$0")"

java -cp bin Client.ClientMain