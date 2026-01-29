package Client;

import Common.Marshaller;
import Common.OperationType;

import java.util.Scanner;

public class ClientMain {
    private final CommunicationLayer comLayer;
    private int requestId = 100; // 初始 Request ID

    public ClientMain(String host, int port) throws Exception {
        this.comLayer = new CommunicationLayer(host, port);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- 分布式银行系统客户端 ---");

        while (true) {
            System.out.println("\n请选择操作:");
            System.out.println("1. 开户  2. 销户  3. 存款  4. 取款  5. 监控更新  6. 查询余额  7. 转账  0. 退出");
            int choice = scanner.nextInt();

            if (choice == 0) break;

            if (choice == 5) { // 监控更新
                handleMonitor(scanner);
            } else {
                handleBankOperation(choice, scanner);
            }
        }
        System.out.println("客户端已关闭。");
    }

    private void handleBankOperation(int choice, Scanner scanner) {
        byte[] requestBuffer = new byte[1024];

        // 1. 写入 Header: RequestID 和 OpCode
        Marshaller.marshalInt(requestId++, requestBuffer, 0);
        Marshaller.marshalInt(choice, requestBuffer, 4);

        // 2. 根据选择封送不同的参数
        switch (choice) {
            case 1: // 开户
                System.out.print("输入姓名: ");
                String name = scanner.next();
                int offset = 8;
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
                break;
                
            case 2: // 销户
                System.out.print("输入账号: ");
                int accNum = scanner.nextInt();
                offset = 8;
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
                offset = 8;
                Marshaller.marshalInt(accNum, requestBuffer, offset);
                offset += 4;
                
                System.out.print("输入密码: ");
                password = scanner.next();
                offset += Marshaller.marshalString(password, requestBuffer, offset);
                
                System.out.print("输入存款金额: ");
                double depositAmount = scanner.nextDouble();
                Marshaller.marshalDouble(depositAmount, requestBuffer, offset);
                break;
                
            case 4: // 取款
                System.out.print("输入账号: ");
                accNum = scanner.nextInt();
                offset = 8;
                Marshaller.marshalInt(accNum, requestBuffer, offset);
                offset += 4;
                
                System.out.print("输入密码: ");
                password = scanner.next();
                offset += Marshaller.marshalString(password, requestBuffer, offset);
                
                System.out.print("输入取款金额: ");
                double withdrawAmount = scanner.nextDouble();
                Marshaller.marshalDouble(withdrawAmount, requestBuffer, offset);
                break;
                
            case 6: // 查询余额
                System.out.print("输入账号: ");
                accNum = scanner.nextInt();
                offset = 8;
                Marshaller.marshalInt(accNum, requestBuffer, offset);
                offset += 4;
                
                System.out.print("输入密码: ");
                password = scanner.next();
                offset += Marshaller.marshalString(password, requestBuffer, offset);
                break;
                
            case 7: // 转账
                System.out.print("输入源账号: ");
                int fromAccNum = scanner.nextInt();
                offset = 8;
                Marshaller.marshalInt(fromAccNum, requestBuffer, offset);
                offset += 4;
                
                System.out.print("输入源账号密码: ");
                password = scanner.next();
                offset += Marshaller.marshalString(password, requestBuffer, offset);
                
                System.out.print("输入目标账号: ");
                int toAccNum = scanner.nextInt();
                Marshaller.marshalInt(toAccNum, requestBuffer, offset);
                offset += 4;
                
                System.out.print("输入转账金额: ");
                double transferAmount = scanner.nextDouble();
                Marshaller.marshalDouble(transferAmount, requestBuffer, offset);
                break;
                
            default:
                System.out.println("无效操作");
                return;
        }

        // 3. 发送并接收回复
        byte[] response = comLayer.sendAndReceive(requestBuffer);
        if (response != null) {
            System.out.println("服务器回复: " + new String(response));
        } else {
            System.out.println("错误: 无法连接到服务器。");
        }
    }

    private void handleMonitor(Scanner scanner) {
        System.out.print("请输入监控时长(秒): ");
        int interval = scanner.nextInt();

        // 发送监控请求给服务器
        byte[] req = new byte[12];
        Marshaller.marshalInt(requestId++, req, 0);
        Marshaller.marshalInt(OperationType.MONITOR_UPDATES.code, req, 4);
        Marshaller.marshalInt(interval, req, 8);

        comLayer.sendAndReceive(req); // 注册
        comLayer.receiveCallback(interval); // 进入阻塞监听状态
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入服务器 IP 地址 (默认 127.0.0.1): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "127.0.0.1";
        
        System.out.println("正在连接到 " + host + ":9800 ...");
        new ClientMain(host, 9800).start();
    }
}