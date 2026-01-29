package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class CallbackManager {
    private static class MonitorClient {
        InetAddress address;
        int port;
        long expiryTime;
    }

    private final List<MonitorClient> clients = new ArrayList<>();

    public synchronized void register(InetAddress addr, int port, int intervalSec) {
        MonitorClient mc = new MonitorClient();
        mc.address = addr;
        mc.port = port;
        mc.expiryTime = System.currentTimeMillis() + (intervalSec * 1000L); // 租约时间
        clients.add(mc);
    }

    public synchronized void notifyUpdate(String updateInfo, DatagramSocket socket) {
        long now = System.currentTimeMillis();
        clients.removeIf(client -> now > client.expiryTime); // 自动移除过期记录

        for (MonitorClient client : clients) {
            try {
                byte[] data = updateInfo.getBytes(); // 发送回调消息 [cite: 47]
                socket.send(new DatagramPacket(data, data.length, client.address, client.port));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}