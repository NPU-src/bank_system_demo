package Client;

import Common.Marshaller;
import Common.OperationType;

import java.util.Scanner;

public class ClientMain {
    private final CommunicationLayer comLayer;
    private int requestId = 100; // 初始 Request ID
    private final Scanner scanner;

    public ClientMain(CommunicationLayer comLayer, Scanner scanner) {
        this.comLayer = comLayer;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("--- 分布式银行系统客户端 / Distributed Banking System Client ---");

        while (true) {
            System.out.println("\n请选择操作 / Please select an operation:");
            System.out.println("1. 开户 / Open Account");
            System.out.println("2. 销户 / Close Account");
            System.out.println("3. 存款 / Deposit");
            System.out.println("4. 取款 / Withdraw");
            System.out.println("5. 监控更新 / Monitor Updates");
            System.out.println("6. 查询余额 / Get Balance");
            System.out.println("7. 转账 / Transfer");
            System.out.println("0. 退出 / Exit");
            
            if (!scanner.hasNextInt()) {
                if (scanner.hasNext()) scanner.next(); // 消耗无效输入 / Consume invalid input
                continue;
            }
            int choice = scanner.nextInt();

            if (choice == 0) break;

            if (choice == 5) { // 监控更新 / Monitor Updates
                handleMonitor();
            } else {
                handleBankOperation(choice);
            }
        }
        System.out.println("客户端已关闭 / Client Closed。");
    }

    private void handleBankOperation(int choice) {
        byte[] requestBuffer = new byte[1024];
        int offset = 0;

        // 1. 写入 Header: RequestID 和 OpCode
        // 1. Write Header: RequestID and OpCode
        Marshaller.marshalInt(requestId++, requestBuffer, offset);
        offset += 4;
        Marshaller.marshalInt(choice, requestBuffer, offset);
        offset += 4;

        // 2. 根据选择封送不同的参数
        // 2. Marshal different parameters based on selection
        try {
            switch (choice) {
                case 1: // 开户 / Open Account
                    System.out.print("输入姓名 / Enter Name: ");
                    String name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);
                    
                    System.out.print("输入密码 / Enter Password: ");
                    String password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("选择货币类型 / Select Currency (0=SGD, 1=USD, 2=EUR, 3=CNY, 4=HKD): ");
                    int currencyIdx = scanner.nextInt();
                    Marshaller.marshalInt(currencyIdx, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入初始余额 / Enter Initial Balance: ");
                    double balance = scanner.nextDouble();
                    Marshaller.marshalDouble(balance, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 2: // 销户 / Close Account
                    System.out.print("输入账号 / Enter Account Number: ");
                    int accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入姓名 / Enter Name: ");
                    name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);
                    
                    System.out.print("输入密码 / Enter Password: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    break;
                    
                case 3: // 存款 / Deposit
                    System.out.print("输入账号 / Enter Account Number: ");
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入姓名 / Enter Name: ");
                    name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);

                    System.out.print("输入密码 / Enter Password: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("选择货币类型 / Select Currency (0=SGD, 1=USD, 2=EUR, 3=CNY, 4=HKD): ");
                    currencyIdx = scanner.nextInt();
                    Marshaller.marshalInt(currencyIdx, requestBuffer, offset);
                    offset += 4;

                    System.out.print("输入存款金额 / Enter Deposit Amount: ");
                    double depositAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(depositAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 4: // 取款 / Withdraw
                    System.out.print("输入账号 / Enter Account Number: ");
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入姓名 / Enter Name: ");
                    name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);

                    System.out.print("输入密码 / Enter Password: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("选择货币类型 / Select Currency (0=SGD, 1=USD, 2=EUR, 3=CNY, 4=HKD): ");
                    currencyIdx = scanner.nextInt();
                    Marshaller.marshalInt(currencyIdx, requestBuffer, offset);
                    offset += 4;

                    System.out.print("输入取款金额 / Enter Withdraw Amount: ");
                    double withdrawAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(withdrawAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 6: // 查询余额 / Get Balance
                    System.out.print("输入账号 / Enter Account Number: ");
                    if (!scanner.hasNextInt()) {
                        System.out.println("错误: 账号必须是数字 / Error: Account number must be numeric.");
                        scanner.next(); // 消耗无效输入 / Consume invalid input
                        return;
                    }
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码 / Enter Password: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    break;
                    
                case 7: // 转账 / Transfer
                    System.out.print("输入源账号 / Enter Source Account: ");
                    int fromAcc = scanner.nextInt();
                    Marshaller.marshalInt(fromAcc, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码 / Enter Password: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("输入目标账号 / Enter Target Account: ");
                    int toAcc = scanner.nextInt();
                    Marshaller.marshalInt(toAcc, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入转账金额 / Enter Transfer Amount: ");
                    double transferAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(transferAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                default:
                    System.out.println("无效操作 / Invalid Operation");
                    return;
            }
        } catch (Exception e) {
            System.out.println("输入错误，请重新选择 / Input Error, please try again.");
            if (scanner.hasNextLine()) scanner.nextLine(); // 清空缓冲区 / Clear buffer
            return;
        }

        // 3. 发送并接收回复
        // 3. Send and Receive Response
        byte[] actualData = new byte[offset];
        System.arraycopy(requestBuffer, 0, actualData, 0, offset);
        
        byte[] response = comLayer.sendAndReceive(actualData);
        if (response != null) {
            try {
                String responseStr = Marshaller.unmarshalString(response, 0);
                System.out.println("服务器回复 / Server Reply: " + responseStr);
            } catch (Exception e) {
                System.out.println("服务器回复 (raw) / Server Reply (raw): " + new String(response));
            }
        } else {
            System.out.println("错误: 请求超时，服务器未响应 / Error: Request timed out, no response from server.");
        }
    }

    private void handleMonitor() {
        System.out.print("请输入监控时长(秒) / Enter monitor duration (seconds): ");
        if (!scanner.hasNextInt()) {
            if (scanner.hasNext()) scanner.next();
            return;
        }
        int interval = scanner.nextInt();

        byte[] req = new byte[12];
        Marshaller.marshalInt(requestId++, req, 0);
        Marshaller.marshalInt(OperationType.MONITOR_UPDATES.code, req, 4);
        Marshaller.marshalInt(interval, req, 8);

        if (comLayer.sendAndReceive(req) != null) {
            comLayer.receiveCallback(interval);
        } else {
            System.out.println("错误: 无法注册监控，服务器超时 / Error: Failed to register monitor, server timeout.");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String host = "127.0.0.1";
        int port = 9800;

        // 1. 尝试从命令行参数读取
        // 1. Try to read from command line args
        if (args.length >= 1) {
            host = args[0];
            if (args.length >= 2) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("错误: 端口号必须是整数。使用默认 9800 / Error: Port must be integer. Using default 9800");
                }
            }
            System.out.println("从命令行参数加载配置 / Loaded config from args: " + host + ":" + port);
        } else {
            // 2. 交互式输入
            // 2. Interactive Input
            System.out.print("请输入服务器 IP 地址 (默认 127.0.0.1) / Enter Server IP (Default 127.0.0.1): ");
            String inputHost = scanner.nextLine().trim();
            if (!inputHost.isEmpty()) host = inputHost;

            System.out.print("请输入服务器端口 (默认 9800) / Enter Server Port (Default 9800): ");
            String inputPort = scanner.nextLine().trim();
            if (!inputPort.isEmpty()) {
                try {
                    port = Integer.parseInt(inputPort);
                } catch (Exception e) {
                    System.out.println("输入无效，使用默认端口 9800 / Invalid input, using default port 9800");
                }
            }
        }
        
        System.out.println("正在初始化网络层 (" + host + ":" + port + ") ... / Initializing Network Layer...");
        CommunicationLayer comLayer = new CommunicationLayer(host, port);
        
        System.out.print("是否进行连接测试? (y/N) / Test connection? (y/N): ");
        if (args.length == 0) { // 仅在未提供参数（交互模式）时询问，或始终询问？为了方便自动化测试，如果提供了参数，我们可以尝试直接 ping 或跳过等待
            // 这里保留询问逻辑，但为了方便，如果直接回车也行
        }
        
        String testChoice = scanner.nextLine().trim();
        if (testChoice.equalsIgnoreCase("y")) {
            System.out.println("正在测试与服务器的连接... / Testing connection to server...");
            if (!comLayer.pingServer()) {
                System.out.println("错误: 无法连接到服务器，请确认服务器已启动并在指定端口监听。 / Error: Cannot connect to server.");
                return;
            }
            System.out.println("成功连接到服务器！ / Connected to server successfully!");
        }
        
        new ClientMain(comLayer, scanner).start();
    }
}
