# 分布式银行系统 (Distributed Banking System)

这是一个基于UDP协议的分布式银行系统，支持多种银行业务操作、两种调用语义以及模拟网络丢包测试。

## 系统特性

- **分布式架构**：客户端和服务器端分离，支持跨网络、跨机器通信。
- **UDP协议**：使用UDP套接字进行所有网络通信。
- **手动封送**：自定义数据序列化/反序列化 (Marshalling/Unmarshalling)，不依赖Java内置序列化。
- **双重调用语义**：支持 **At-Most-Once** (默认) 和 **At-Least-Once** 语义切换。
- **实时监控**：支持客户端订阅服务器更新通知 (Callback)。
- **模拟丢包**：内置 **30% 随机丢包率** (Simulated Packet Loss) 用于测试系统的容错性和重传机制。

## 功能列表

### 1. 开户 (Open Account)
- 输入：姓名、密码、货币类型、初始余额
- 输出：新生成的账号

### 2. 销户 (Close Account)
- 输入：账号、姓名、密码
- 输出：销户结果

### 3. 存款 (Deposit)
- 输入：账号、**姓名**、密码、**货币类型**、存款金额
- 输出：新的余额信息

### 4. 取款 (Withdraw)
- 输入：账号、**姓名**、密码、**货币类型**、取款金额
- 输出：新的余额信息

### 5. 查询余额 (Get Balance) - 幂等操作
- 输入：账号、密码
- 输出：当前余额信息

### 6. 转账 (Transfer) - 非幂等操作
- 输入：源账号、密码、目标账号、转账金额
- 输出：转账结果

### 7. 监控更新 (Monitor Updates)
- 输入：监控时长（秒）
- 功能：在指定时间内接收服务器的所有更新通知

## 环境要求
- Java JDK 25 (或兼容版本)
- 支持 UDP 通信的网络环境

## 编译和运行

### 编译
```bash
mkdir -p out
javac -d out -sourcepath src src/Common/*.java src/Server/*.java src/Client/*.java
```

### 启动服务器
支持指定监听端口和调用语义。

**语法**: `java Server.ServerMain [Port] [Semantics]`

- `Port`: 服务器监听端口 (默认 9800)
- `Semantics`: `AMO` (At-Most-Once, 默认) 或 `ALO` (At-Least-Once)

**示例**:
```bash
# 使用默认设置 (端口 9800, At-Most-Once)
java -cp out Server.ServerMain

# 指定端口 12345
java -cp out Server.ServerMain 12345

# 指定端口 12345 并使用 At-Least-Once 语义
java -cp out Server.ServerMain 12345 ALO
```

### 启动客户端
支持指定服务器 IP 和端口。

**语法**: `java Client.ClientMain [ServerIP] [ServerPort]`

- `ServerIP`: 服务器 IP 地址 (默认 127.0.0.1)
- `ServerPort`: 服务器端口 (默认 9800)

**示例**:
```bash
# 交互式模式 (启动后询问 IP/Port)
java -cp out Client.ClientMain

# 指定连接本地服务器
java -cp out Client.ClientMain 127.0.0.1 9800

# 指定连接远程服务器 (例如实验室伙伴的机器)
java -cp out Client.ClientMain 192.168.1.50 9800
```

---

# English Translation

# Distributed Banking System

This is a distributed banking system based on the UDP protocol, supporting various banking operations, two invocation semantics, and simulated network packet loss.

## System Features

- **Distributed Architecture**: Separation of client and server, supporting communication across networks and machines.
- **UDP Protocol**: Uses UDP sockets for all network communication.
- **Manual Marshalling**: Custom data serialization/deserialization (Marshalling/Unmarshalling) without relying on Java's built-in serialization.
- **Dual Invocation Semantics**: Supports **At-Most-Once** (default) and **At-Least-Once** semantics.
- **Real-time Monitoring**: Supports client subscription to server update notifications (Callback).
- **Simulated Packet Loss**: Built-in **30% random packet loss rate** to test the system's fault tolerance and retransmission mechanisms.

## Function List

### 1. Open Account
- Input: Name, Password, Currency Type, Initial Balance
- Output: Newly generated Account Number

### 2. Close Account
- Input: Account Number, Name, Password
- Output: Closing Result

### 3. Deposit
- Input: Account Number, **Name**, Password, **Currency Type**, Deposit Amount
- Output: New Balance Information

### 4. Withdraw
- Input: Account Number, **Name**, Password, **Currency Type**, Withdrawal Amount
- Output: New Balance Information

### 5. Get Balance (Idempotent Operation)
- Input: Account Number, Password
- Output: Current Balance Information

### 6. Transfer (Non-idempotent Operation)
- Input: Source Account Number, Password, Target Account Number, Transfer Amount
- Output: Transfer Result

### 7. Monitor Updates
- Input: Monitor Duration (seconds)
- Function: Receive all server update notifications within the specified time

## Requirements
- Java JDK 25 (or compatible version)
- Network environment supporting UDP communication

## Compilation and Execution

### Compile
```bash
mkdir -p out
javac -d out -sourcepath src src/Common/*.java src/Server/*.java src/Client/*.java
```

### Start Server
Supports specifying the listening port and invocation semantics.

**Syntax**: `java Server.ServerMain [Port] [Semantics]`

- `Port`: Server listening port (default 9800)
- `Semantics`: `AMO` (At-Most-Once, default) or `ALO` (At-Least-Once)

**Examples**:
```bash
# Use default settings (Port 9800, At-Most-Once)
java -cp out Server.ServerMain

# Specify port 12345
java -cp out Server.ServerMain 12345

# Specify port 12345 and use At-Least-Once semantics
java -cp out Server.ServerMain 12345 ALO
```

### Start Client
Supports specifying the Server IP and Port.

**Syntax**: `java Client.ClientMain [ServerIP] [ServerPort]`

- `ServerIP`: Server IP address (default 127.0.0.1)
- `ServerPort`: Server port (default 9800)

**Examples**:
```bash
# Interactive mode (Ask for IP/Port after start)
java -cp out Client.ClientMain

# Connect to local server
java -cp out Client.ClientMain 127.0.0.1 9800

# Connect to remote server (e.g., lab partner's machine)
java -cp out Client.ClientMain 192.168.1.50 9800
```
