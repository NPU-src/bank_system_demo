package Client;

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

    // 发送请求并等待回复，包含超时重传逻辑
    public byte[] sendAndReceive(byte[] requestData) {
        int retryCount = 0;
        byte[] receiveBuf = new byte[2048];

        while (retryCount < MAX_RETRIES) {
            try {
                DatagramPacket sendPacket = new DatagramPacket(
                        requestData, requestData.length, serverAddress, serverPort);
                socket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                socket.receive(receivePacket); // 阻塞等待回复 [cite: 29]

                // 成功收到回复
                byte[] response = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuf, 0, response, 0, receivePacket.getLength());
                return response;

            } catch (SocketTimeoutException e) {
                retryCount++;
                System.out.println("请求超时，正在进行第 " + retryCount + " 次重试...");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return null; // 多次尝试后依然失败
    }

    // 专门用于监控模式：只接收，不主动发送 [cite: 47, 49]
    public void receiveCallback(int intervalSec) {
        long endTime = System.currentTimeMillis() + (intervalSec * 1000L);
        byte[] buffer = new byte[1024];

        System.out.println("进入监控模式，时长：" + intervalSec + "秒...");
        try {
            while (System.currentTimeMillis() < endTime) {
                // 计算剩余的超时时间
                int remaining = (int) (endTime - System.currentTimeMillis());
                if (remaining <= 0) break;

                socket.setSoTimeout(remaining);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String update = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("[监控更新]: " + update); // [cite: 47]
                } catch (SocketTimeoutException e) {
                    break; // 监控时间到
                }
            }
        } catch (Exception e) {
            System.out.println("监控结束。");
        }
    }
}