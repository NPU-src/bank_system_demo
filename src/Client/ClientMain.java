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
        System.out.println("--- 分布式银行系统客户端 ---");

        while (true) {
            System.out.println("\n请选择操作:");
            System.out.println("1. 开户  2. 销户  3. 存款  4. 取款  5. 监控更新  6. 查询余额  7. 转账  0. 退出");
            
            if (!scanner.hasNextInt()) {
                if (scanner.hasNext()) scanner.next(); // 消耗无效输入
                continue;
            }
            int choice = scanner.nextInt();

            if (choice == 0) break;

            if (choice == 5) { // 监控更新
                handleMonitor();
            } else {
                handleBankOperation(choice);
            }
        }
        System.out.println("客户端已关闭。");
    }

    private void handleBankOperation(int choice) {
        byte[] requestBuffer = new byte[1024];
        int offset = 0;

        // 1. 写入 Header: RequestID 和 OpCode
        Marshaller.marshalInt(requestId++, requestBuffer, offset);
        offset += 4;
        Marshaller.marshalInt(choice, requestBuffer, offset);
        offset += 4;

        // 2. 根据选择封送不同的参数
        try {
            switch (choice) {
                case 1: // 开户
                    System.out.print("输入姓名: ");
                    String name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);
                    
                    System.out.print("输入密码: ");
                    String password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("选择货币类型 (0=SGD, 1=USD, 2=EUR, 3=CNY, 4=HKD): ");
                    int currencyIdx = scanner.nextInt();
                    Marshaller.marshalInt(currencyIdx, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入初始余额: ");
                    double balance = scanner.nextDouble();
                    Marshaller.marshalDouble(balance, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 2: // 销户
                    System.out.print("输入账号: ");
                    int accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入姓名: ");
                    name = scanner.next();
                    offset += Marshaller.marshalString(name, requestBuffer, offset);
                    
                    System.out.print("输入密码: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    break;
                    
                case 3: // 存款
                    System.out.print("输入账号: ");
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("输入存款金额: ");
                    double depositAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(depositAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 4: // 取款
                    System.out.print("输入账号: ");
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("输入取款金额: ");
                    double withdrawAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(withdrawAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                case 6: // 查询余额
                    System.out.print("输入账号: ");
                    if (!scanner.hasNextInt()) {
                        System.out.println("错误: 账号必须是数字。");
                        scanner.next(); // 消耗无效输入
                        return;
                    }
                    accNum = scanner.nextInt();
                    Marshaller.marshalInt(accNum, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    break;
                    
                case 7: // 转账
                    System.out.print("输入源账号: ");
                    int fromAcc = scanner.nextInt();
                    Marshaller.marshalInt(fromAcc, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入密码: ");
                    password = scanner.next();
                    offset += Marshaller.marshalString(password, requestBuffer, offset);
                    
                    System.out.print("输入目标账号: ");
                    int toAcc = scanner.nextInt();
                    Marshaller.marshalInt(toAcc, requestBuffer, offset);
                    offset += 4;
                    
                    System.out.print("输入转账金额: ");
                    double transferAmount = scanner.nextDouble();
                    Marshaller.marshalDouble(transferAmount, requestBuffer, offset);
                    offset += 8;
                    break;
                    
                default:
                    System.out.println("无效操作");
                    return;
            }
        } catch (Exception e) {
            System.out.println("输入错误，请重新选择。");
            if (scanner.hasNextLine()) scanner.nextLine(); // 清空缓冲区
            return;
        }

        // 3. 发送并接收回复
        byte[] actualData = new byte[offset];
        System.arraycopy(requestBuffer, 0, actualData, 0, offset);
        
        byte[] response = comLayer.sendAndReceive(actualData);
        if (response != null) {
            try {
                String responseStr = Marshaller.unmarshalString(response, 0);
                System.out.println("服务器回复: " + responseStr);
            } catch (Exception e) {
                System.out.println("服务器回复 (raw): " + new String(response));
            }
        } else {
            System.out.println("错误: 请求超时，服务器未响应。");
        }
    }

    private void handleMonitor() {
        System.out.print("请输入监控时长(秒): ");
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
            System.out.println("错误: 无法注册监控，服务器超时。");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入服务器 IP 地址 (默认 192.168.1.75, 局域网请输入服务器实际IP如 192.168.1.100，直接按回车使用默认值): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "192.168.1.75";
        } else if (host.equals("9800")) {
            System.out.println("检测到输入了端口号 9800，已自动更正为 192.168.1.75");
            host = "192.168.1.75";
        }
        
        System.out.println("正在初始化网络层 (" + host + ":9800) ...");
        CommunicationLayer comLayer = new CommunicationLayer(host, 9800);
        
        System.out.print("是否进行连接测试? (y/N): ");
        String testChoice = scanner.nextLine().trim();
        if (testChoice.equalsIgnoreCase("y")) {
            System.out.println("正在测试与服务器的连接...");
            if (!comLayer.pingServer()) {
                System.out.println("错误: 无法连接到服务器，请确认服务器已启动并在指定端口监听。");
                return;
            }
            System.out.println("成功连接到服务器！");
        }
        
        new ClientMain(comLayer, scanner).start();
    }
}
