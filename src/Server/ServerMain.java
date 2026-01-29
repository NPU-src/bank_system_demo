package Server;

import Common.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerMain {
    private final DatagramSocket socket;
    private final BankStore store = new BankStore();
    private final RequestHistory history = new RequestHistory();
    private final CallbackManager callbackManager = new CallbackManager();
    private final boolean atMostOnce;

    public ServerMain(int port, boolean atMostOnce) throws Exception {
        this.socket = new DatagramSocket(port);
        this.atMostOnce = atMostOnce;
    }

    public void start() throws Exception {
        System.out.println("服务器已启动，监听端口: " + socket.getLocalPort());
        System.out.println("当前调用语义: " + (atMostOnce ? "At-Most-Once" : "At-Least-Once"));
        byte[] receiveBuf = new byte[2048];

        while (true) {
            DatagramPacket requestPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            socket.receive(requestPacket);

            // 1. 手动解析 Header (RequestID, OpCode)
            int reqId = Marshaller.unmarshalInt(receiveBuf, 0);
            int opCode = Marshaller.unmarshalInt(receiveBuf, 4);
            String clientKey = requestPacket.getAddress().getHostAddress() + ":" + requestPacket.getPort();

            // 2. 打印收到的请求
            System.out.println("[Request] From: " + clientKey + " ID: " + reqId + " OpCode: " + opCode);

            // 3. 模拟丢包实验 (用于撰写实验报告)
            if (Math.random() < 0.1) {
                System.out.println("   (模拟丢包：丢弃该请求)");
                continue;

            }

            // 4. At-most-once 逻辑：检查历史
            if (atMostOnce) {
                byte[] cached = history.getResponse(clientKey, reqId);
                if (cached != null) {
                    System.out.println("   (重复请求，发送缓存结果)");
                    socket.send(new DatagramPacket(cached, cached.length, requestPacket.getAddress(), requestPacket.getPort()));
                    continue;
                }
            }

            // 5. 执行逻辑
            String responseStr;
            OperationType op = OperationType.fromInt(opCode);
            boolean isUpdate = false;

            if (op == null) {
                responseStr = "错误: 未知操作";
            } else {
                try {
                    switch (op) {
                        case OPEN_ACCOUNT: {
                            String name = Marshaller.unmarshalString(receiveBuf, 8);
                            int offset = 8 + Marshaller.getStringEncodedLength(name);
                            String password = Marshaller.unmarshalString(receiveBuf, offset);
                            offset += Marshaller.getStringEncodedLength(password);
                            int currencyIdx = Marshaller.unmarshalInt(receiveBuf, offset);
                            offset += 4;
                            double balance = Marshaller.unmarshalDouble(receiveBuf, offset);
                            
                            int accNum = store.openAccount(name, password, CurrencyType.fromInt(currencyIdx), balance);
                            responseStr = "成功: 开户完成。账号为: " + accNum;
                            isUpdate = true;
                            break;
                        }
                        case CLOSE_ACCOUNT: {
                            int accNum = Marshaller.unmarshalInt(receiveBuf, 8);
                            String name = Marshaller.unmarshalString(receiveBuf, 12);
                            int offset = 12 + Marshaller.getStringEncodedLength(name);
                            String password = Marshaller.unmarshalString(receiveBuf, offset);
                            
                            responseStr = store.closeAccount(accNum, name, password);
                            if (responseStr.startsWith("成功")) isUpdate = true;
                            break;
                        }
                        case DEPOSIT: {
                            int accNum = Marshaller.unmarshalInt(receiveBuf, 8);
                            String password = Marshaller.unmarshalString(receiveBuf, 12);
                            int offset = 12 + Marshaller.getStringEncodedLength(password);
                            double amount = Marshaller.unmarshalDouble(receiveBuf, offset);
                            
                            responseStr = store.deposit(accNum, password, amount);
                            if (responseStr.startsWith("成功")) isUpdate = true;
                            break;
                        }
                        case WITHDRAW: {
                            int accNum = Marshaller.unmarshalInt(receiveBuf, 8);
                            String password = Marshaller.unmarshalString(receiveBuf, 12);
                            int offset = 12 + Marshaller.getStringEncodedLength(password);
                            double amount = Marshaller.unmarshalDouble(receiveBuf, offset);
                            
                            responseStr = store.withdraw(accNum, password, amount);
                            if (responseStr.startsWith("成功")) isUpdate = true;
                            break;
                        }
                        case MONITOR_UPDATES: {
                            int interval = Marshaller.unmarshalInt(receiveBuf, 8);
                            callbackManager.register(requestPacket.getAddress(), requestPacket.getPort(), interval);
                            responseStr = "成功: 已注册监控，时长 " + interval + " 秒";
                            break;
                        }
                        case GET_BALANCE: {
                            int accNum = Marshaller.unmarshalInt(receiveBuf, 8);
                            String password = Marshaller.unmarshalString(receiveBuf, 12);
                            
                            responseStr = store.getBalance(accNum, password);
                            break;
                        }
                        case TRANSFER: {
                            int fromAcc = Marshaller.unmarshalInt(receiveBuf, 8);
                            String password = Marshaller.unmarshalString(receiveBuf, 12);
                            int offset = 12 + Marshaller.getStringEncodedLength(password);
                            int toAcc = Marshaller.unmarshalInt(receiveBuf, offset);
                            offset += 4;
                            double amount = Marshaller.unmarshalDouble(receiveBuf, offset);
                            
                            responseStr = store.transfer(fromAcc, password, toAcc, amount);
                            if (responseStr.startsWith("成功")) isUpdate = true;
                            break;
                        }
                        default:
                            responseStr = "错误: 尚未实现的业务逻辑";
                    }
                } catch (Exception e) {
                    responseStr = "错误: 解析请求失败 - " + e.getMessage();
                }
            }

            // 6. 处理回调通知
            if (isUpdate) {
                callbackManager.notifyUpdate("[系统更新] " + responseStr, socket);
            }

            // 7. 保存历史并发送回复
            byte[] responseBytes = new byte[2048]; // 足够大的 buffer
            int responseLen = Marshaller.marshalString(responseStr, responseBytes, 0);
            byte[] actualResponse = new byte[responseLen];
            System.arraycopy(responseBytes, 0, actualResponse, 0, responseLen);

            if (atMostOnce) history.saveResponse(clientKey, reqId, actualResponse);
            socket.send(new DatagramPacket(actualResponse, actualResponse.length, requestPacket.getAddress(), requestPacket.getPort()));
            System.out.println("[Reply] Sent to " + clientKey + ": " + responseStr);
        }
    }

    public static void main(String[] args) throws Exception {
        boolean amo = true;
        if (args.length > 0 && args[0].equalsIgnoreCase("ALO")) {
            amo = false;
        }
        // 如果没有参数，也可以从控制台询问（可选）
        // 这里为了简单，默认 At-Most-Once，可以通过参数 "ALO" 切换
        System.out.println("使用说明: java Server.ServerMain [ALO]");
        System.out.println("   ALO: 使用 At-Least-Once 语义");
        System.out.println("   默认: 使用 At-Most-Once 语义");
        
        new ServerMain(9800, amo).start();
    }
}