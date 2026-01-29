package Server;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于实现 At-most-once 语义，记录已处理的请求结果
 */
public class RequestHistory {
    // Key: "客户端IP:端口:RequestID"
    private final Map<String, byte[]> history = new HashMap<>();

    public void saveResponse(String clientInfo, int requestId, byte[] response) {
        history.put(clientInfo + ":" + requestId, response);
    }

    public byte[] getResponse(String clientInfo, int requestId) {
        return history.get(clientInfo + ":" + requestId);
    }
}