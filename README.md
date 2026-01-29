# 分布式银行系统

这是一个基于UDP协议的分布式银行系统，支持多种银行业务操作和两种调用语义。

## 系统特性

- **分布式架构**：客户端和服务器端分离，支持跨网络通信
- **UDP协议**：使用UDP套接字进行所有网络通信
- **手动封送**：自定义数据序列化/反序列化，不依赖Java内置序列化
- **双重调用语义**：支持At-Most-Once和At-Least-Once语义
- **实时监控**：支持客户端订阅服务器更新通知

## 功能列表

### 1. 开户 (Open Account)
- 输入：姓名、密码、货币类型、初始余额
- 输出：新生成的账号

### 2. 存款 (Deposit)
- 输入：账号、密码、存款金额
- 输出：新的余额信息

### 3. 取款 (Withdraw)
- 输入：账号、密码、取款金额
- 输出：新的余额信息

### 4. 销户 (Close Account)
- 输入：账号、姓名、密码
- 输出：销户结果

### 5. 查询余额 (Get Balance) - 幂等操作
- 输入：账号、密码
- 输出：当前余额信息

### 6. 转账 (Transfer) - 非幂等操作
- 输入：源账号、密码、目标账号、转账金额
- 输出：转账结果

### 7. 监控更新 (Monitor Updates)
- 输入：监控时长（秒）
- 功能：在指定时间内接收服务器的所有更新通知

## 调用语义

### At-Most-Once (至多一次)
- 服务器记录已处理请求的历史
- 对于重复的Request ID，直接返回缓存结果
- 防止重复操作（如重复扣款）

### At-Least-Once (至少一次)
- 客户端在未收到响应时会自动重传
- 确保请求最终会被处理

## 编译和运行

### 编译
```bash
mkdir -p bin
javac -d bin src/Common/*.java src/Server/*.java src/Client/*.java
```

### 启动服务器
```bash
# At-Most-Once 模式（默认）
java -cp bin Server.ServerMain

# At-Least-Once 模式
java -cp bin Server.ServerMain ALO
```

### 启动客户端
```bash
java -cp bin Client.ClientMain
```

或者使用提供的脚本：
```bash
# 启动服务器
chmod +x run_server.sh
./run_server.sh        # At-Most-Once
./run_server.sh ALO    # At-Least-Once

# 启动客户端
chmod +x run_client.sh
./run_client.sh
```

## 快速开始示例

1. 首先编译项目：
   ```bash
   mkdir -p bin
   javac -d bin src/Common/*.java src/Server/*.java src/Client/*.java
   ```

2. 在一个终端窗口启动服务器：
   ```bash
   java -cp bin Server.ServerMain
   ```

3. 在另一个终端窗口启动客户端：
   ```bash
   java -cp bin Client.ClientMain
   ```

4. 在客户端中进行操作：
   - 选择 1 开户
   - 选择 3 存款
   - 选择 4 取款
   - 选择 6 查询余额
   - 等等...

## 项目结构

```
src/
├── Client/
│   ├── ClientMain.java     # 客户端主程序
│   └── CommunicationLayer.java  # UDP通信层
├── Common/
│   ├── Account.java        # 账户实体
│   ├── CurrencyType.java   # 货币类型枚举
│   ├── Marshaller.java     # 数据封送/解送工具
│   └── OperationType.java  # 操作类型枚举
└── Server/
    ├── BankStore.java      # 银行数据存储
    ├── CallbackManager.java # 回调管理器
    ├── RequestHistory.java  # 请求历史记录
    └── ServerMain.java     # 服务器主程序
```

## 技术细节

### 数据封送协议
- **整数 (int)**：4字节，大端序
- **浮点数 (double)**：8字节，IEEE 754标准
- **字符串**：4字节长度 + 实际内容字节

### 消息格式
```
[4字节 RequestID][4字节 OpCode][参数...]
```

### 支持的货币类型
- SGD (新加坡元)
- USD (美元)
- EUR (欧元)
- CNY (人民币)
- HKD (港币)