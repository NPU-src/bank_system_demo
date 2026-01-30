package Client;

import Common.Marshaller;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class CommunicationLayer {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private static final int TIMEOUT = 2000; // 2秒超时
    private static final int MAX_RETRIES = 3; // 最大重试次数

    public CommunicationLayer(String host, int port) throws Exception {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(TIMEOUT);
        this.serverAddress = InetAddress.getByName(host);
        this.serverPort = port;
    }

    private void drainBuffer() {
        byte[] dummy = new byte[2048];
        DatagramPacket packet = new DatagramPacket(dummy, dummy.length);
        try {
            int oldTimeout = socket.getSoTimeout();
            socket.setSoTimeout(1); // 极短超时
            while (true) {
                socket.receive(packet);
            }
        } catch (Exception e) {
            // 缓冲区已空
        } finally {
            try { socket.setSoTimeout(TIMEOUT); } catch (Exception e) {}
        }
    }

    public boolean pingServer() {
        byte[] pingData = new byte[8];
        Marshaller.marshalInt(-1, pingData, 0); // ReqID
        Marshaller.marshalInt(-1, pingData, 4); // OpCode
        
        byte[] response = sendAndReceive(pingData);
        return response != null;
    }

    public byte[] sendAndReceive(byte[] requestData) {
        int expectedId = Marshaller.unmarshalInt(requestData, 0);
        int retryCount = 0;
        byte[] receiveBuf = new byte[2048];

        while (retryCount < MAX_RETRIES) {
            try {
                // 1. 清理可能残留的旧数据包
                drainBuffer();

                // 2. 发送请求
                DatagramPacket sendPacket = new DatagramPacket(
                        requestData, requestData.length, serverAddress, serverPort);
                socket.send(sendPacket);

                // 3. 循环接收，直到收到匹配的 ID 或超时
                long startTime = System.currentTimeMillis();
                while (true) {
                    long timeLeft = TIMEOUT - (System.currentTimeMillis() - startTime);
                    if (timeLeft <= 0) throw new SocketTimeoutException();
                    
                    socket.setSoTimeout((int)timeLeft);
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                    socket.receive(receivePacket);

                    if (receivePacket.getLength() < 4) continue;

                    int receivedId = Marshaller.unmarshalInt(receiveBuf, 0);
                    if (receivedId == expectedId) {
                        // 匹配成功，提取数据
                        int dataLen = receivePacket.getLength() - 4;
                        byte[] response = new byte[dataLen];
                        System.arraycopy(receiveBuf, 4, response, 0, dataLen);
                        return response;
                    } else {
                        System.out.println("   [Skip] 收到无关响应 (ID:" + receivedId + ")，继续等待...");
                    }
                }

            } catch (SocketTimeoutException e) {
                retryCount++;
                System.out.println("请求超时 (ID:" + expectedId + ")，正在进行第 " + retryCount + " 次重试...");
            } catch (Exception e) {
                System.err.println("网络层错误: " + e.getMessage());
                retryCount++;
            }
        }
        return null;
    }

    public void receiveCallback(int durationSeconds) {
        System.out.println("--- 进入监控模式，时长 " + durationSeconds + " 秒 ---");
        byte[] buf = new byte[2048];
        long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

        try {
            while (System.currentTimeMillis() < endTime) {
                int timeLeft = (int)(endTime - System.currentTimeMillis());
                socket.setSoTimeout(Math.max(1, timeLeft));
                
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(p);
                    // 回调通知通常只有字符串
                    String msg = Marshaller.unmarshalString(buf, 0);
                    System.out.println("\n" + msg);
                } catch (SocketTimeoutException e) {
                    // 正常超时，继续循环检查时间
                }
            }
        } catch (Exception e) {
            System.err.println("监控结束或发生错误: " + e.getMessage());
        } finally {
            System.out.println("--- 监控结束 ---");
        }
    }
}
