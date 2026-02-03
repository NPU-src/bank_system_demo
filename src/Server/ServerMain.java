package Server;

import Common.CurrencyType;
import Common.Marshaller;
import Common.OperationType;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerMain {
    private DatagramSocket socket;
    private final BankStore store;
    private final RequestHistory history;
    private final CallbackManager callbackManager;
    private final boolean atMostOnce;
    private static final double LOSS_RATE = 0.3; // [模拟丢包] 30% 丢包率

    public ServerMain(int port, boolean atMostOnce) throws Exception {
        try {
            this.socket = new DatagramSocket(port);
        } catch (java.net.BindException e) {
            System.err.println("错误: 端口 " + port + " 已被占用。请检查是否有其他服务器实例正在运行。");
            throw e;
        }
        this.store = new BankStore();
        this.history = new RequestHistory();
        this.callbackManager = new CallbackManager();
        this.atMostOnce = atMostOnce;
    }

    public void start() throws Exception {
        System.out.println("服务器已启动，监听端口: " + socket.getLocalPort());
        System.out.println("当前调用语义: " + (atMostOnce ? "At-Most-Once" : "At-Least-Once"));
        byte[] receiveBuf = new byte[2048];

        while (true) {
            try {
                DatagramPacket requestPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                socket.receive(requestPacket);

                // 1. 手动解析 Header (RequestID, OpCode)
                if (requestPacket.getLength() < 8) {
                    System.err.println("收到非法短包，长度: " + requestPacket.getLength());
                    continue;
                }
                int reqId = Marshaller.unmarshalInt(receiveBuf, 0);
                int opCode = Marshaller.unmarshalInt(receiveBuf, 4);
                String clientKey = requestPacket.getAddress().getHostAddress() + ":" + requestPacket.getPort();

                // 2. 打印收到的请求
                System.out.println("[Request] From: " + clientKey + " ID: " + reqId + " OpCode: " + opCode);

                // 3. At-most-once 逻辑：检查历史
                if (atMostOnce) {
                    byte[] cached = history.getResponse(clientKey, reqId);
                    if (cached != null) {
                        System.out.println("   (重复请求，发送缓存结果)");
                        socket.send(new DatagramPacket(cached, cached.length, requestPacket.getAddress(), requestPacket.getPort()));
                        continue;
                    }
                }

                // 4. 执行业务逻辑
                String responseStr = "";
                boolean isUpdate = false;

                if (opCode == -1) {
                    responseStr = "PONG";
                } else {
                    OperationType op = OperationType.fromInt(opCode);
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
                                    String name = Marshaller.unmarshalString(receiveBuf, 12);
                                    int offset = 12 + Marshaller.getStringEncodedLength(name);
                                    String password = Marshaller.unmarshalString(receiveBuf, offset);
                                    offset += Marshaller.getStringEncodedLength(password);
                                    int currencyIdx = Marshaller.unmarshalInt(receiveBuf, offset);
                                    offset += 4;
                                    double amount = Marshaller.unmarshalDouble(receiveBuf, offset);
                                    
                                    responseStr = store.deposit(accNum, name, password, CurrencyType.fromInt(currencyIdx), amount);
                                    if (responseStr.startsWith("成功")) isUpdate = true;
                                    break;
                                }
                                case WITHDRAW: {
                                    int accNum = Marshaller.unmarshalInt(receiveBuf, 8);
                                    String name = Marshaller.unmarshalString(receiveBuf, 12);
                                    int offset = 12 + Marshaller.getStringEncodedLength(name);
                                    String password = Marshaller.unmarshalString(receiveBuf, offset);
                                    offset += Marshaller.getStringEncodedLength(password);
                                    int currencyIdx = Marshaller.unmarshalInt(receiveBuf, offset);
                                    offset += 4;
                                    double amount = Marshaller.unmarshalDouble(receiveBuf, offset);
                                    
                                    responseStr = store.withdraw(accNum, name, password, CurrencyType.fromInt(currencyIdx), amount);
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
                            responseStr = "错误: 解析业务失败 - " + e.getMessage();
                        }
                    }
                }

                // 5. 回调通知
                if (isUpdate) {
                    callbackManager.notifyUpdate("[系统更新] " + responseStr, socket);
                }

                // 6. 构造响应包: [RequestID (4字节)] + [ResponseString (封送)]
                byte[] responseBytes = new byte[2048];
                Marshaller.marshalInt(reqId, responseBytes, 0); // 必须带上请求ID
                int strLen = Marshaller.marshalString(responseStr, responseBytes, 4);
                int totalLen = 4 + strLen;
                
                byte[] actualResponse = new byte[totalLen];
                System.arraycopy(responseBytes, 0, actualResponse, 0, totalLen);

                if (atMostOnce) history.saveResponse(clientKey, reqId, actualResponse);

                // [模拟丢包] 随机决定是否真正发送回复
                if (Math.random() < LOSS_RATE) {
                    System.out.println("   [模拟丢包] 响应包已在服务器被丢弃 (ReqID:" + reqId + ")");
                } else {
                    socket.send(new DatagramPacket(actualResponse, actualResponse.length, requestPacket.getAddress(), requestPacket.getPort()));
                    System.out.println("[Reply] Sent to " + clientKey + " ID: " + reqId + ": " + responseStr);
                }

            } catch (Exception e) {
                System.err.println("服务器运行错误: " + e.getMessage());
                e.printStackTrace();
                // 继续运行，不退出循环
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9800;
        boolean amo = true;

        // 参数解析: [Port] [ALO]
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // 如果第一个参数不是数字，检查是不是 ALO/AMO
                if (args[0].equalsIgnoreCase("ALO")) amo = false;
                else if (args[0].equalsIgnoreCase("AMO")) amo = true;
                else System.err.println("警告: 无法识别的参数 '" + args[0] + "'，使用默认端口 9800");
            }
        }

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("ALO")) amo = false;
            else if (args[1].equalsIgnoreCase("AMO")) amo = true;
        }

        System.out.println("使用说明: java Server.ServerMain [Port] [ALO]");
        
        new ServerMain(port, amo).start();
    }
}
