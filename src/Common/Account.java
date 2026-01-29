package Common;

public class Account {
    private int accountNumber;        // 账号（整数）
    private String name;             // 户名（变长字符串）
    private String password;         // 密码（固定长度字符串）
    private CurrencyType currency;   // 货币类型（枚举类型）
    private double balance;          // 余额（浮点值）

    public Account(int accountNumber, String name, String password, CurrencyType currency, double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.password = password;
        this.currency = currency;
        this.balance = balance;
    }

    // 提供 Getter 和 Setter 供服务器业务逻辑使用
    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public CurrencyType getCurrency() { return currency; }
    public double getBalance() { return balance; }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // 辅助方法：打印账户信息，用于满足服务器的回显要求 [cite: 71]
    @Override
    public String toString() {
        return String.format("账号:%d | 户名:%s | 余额:%.2f %s",
                accountNumber, name, balance, currency);
    }
}