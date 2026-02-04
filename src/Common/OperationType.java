package Common;

public enum OperationType {
    OPEN_ACCOUNT(1),       // 开户 / Open Account
    CLOSE_ACCOUNT(2),      // 销户 / Close Account
    DEPOSIT(3),            // 存款 / Deposit
    WITHDRAW(4),           // 取款 / Withdraw
    MONITOR_UPDATES(5),    // 监控 / Monitor Updates
    GET_BALANCE(6),        // 自定义：幂等（查询余额）/ Custom: Idempotent (Get Balance)
    TRANSFER(7);           // 自定义：非幂等（转账）/ Custom: Non-idempotent (Transfer)

    public final int code;
    OperationType(int code) { this.code = code; }

    public static OperationType fromInt(int i) {
        for (OperationType t : values()) if (t.code == i) return t;
        return null;
    }
}