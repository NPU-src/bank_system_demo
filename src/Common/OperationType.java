package Common;

public enum OperationType {
    OPEN_ACCOUNT(1),       // 开户
    CLOSE_ACCOUNT(2),      // 销户
    DEPOSIT(3),            // 存款
    WITHDRAW(4),           // 取款
    MONITOR_UPDATES(5),    // 监控
    GET_BALANCE(6),        // 自定义：幂等（查询余额）
    TRANSFER(7);           // 自定义：非幂等（转账）

    public final int code;
    OperationType(int code) { this.code = code; }

    public static OperationType fromInt(int i) {
        for (OperationType t : values()) if (t.code == i) return t;
        return null;
    }
}