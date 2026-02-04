package Server;

import Common.Account;
import Common.CurrencyType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BankStore {
    // 存储所有账户信息 / Store all account information
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger accountNumberCounter = new AtomicInteger(1000);

    public synchronized int openAccount(String name, String password, CurrencyType currency, double balance) {
        int accNum = accountNumberCounter.getAndIncrement();
        accounts.put(accNum, new Account(accNum, name, password, currency, balance));
        return accNum;
    }

    public synchronized String closeAccount(int accNum, String name, String password) {
        Account acc = accounts.get(accNum);
        if (acc == null) return "错误: 账户不存在 / Error: Account not found";
        if (!acc.getName().equals(name) || !acc.getPassword().equals(password)) {
            return "错误: 身份验证失败 / Error: Authentication failed";
        }
        accounts.remove(accNum);
        return "成功: 账户已关闭 / Success: Account closed";
    }

    public synchronized String deposit(int accNum, String name, String password, CurrencyType currency, double amount) {
        Account acc = accounts.get(accNum);
        if (acc == null) return "错误: 账户不存在 / Error: Account not found";
        if (!acc.getName().equals(name) || !acc.getPassword().equals(password)) return "错误: 身份验证失败 / Error: Authentication failed";
        if (acc.getCurrency() != currency) return "错误: 货币类型不匹配 / Error: Currency mismatch";
        
        acc.setBalance(acc.getBalance() + amount);
        return String.format("成功: 存款完成。新余额为: %.2f / Success: Deposit completed. New balance: %.2f", acc.getBalance(), acc.getBalance());
    }

    public synchronized String withdraw(int accNum, String name, String password, CurrencyType currency, double amount) {
        Account acc = accounts.get(accNum);
        if (acc == null) return "错误: 账户不存在 / Error: Account not found";
        if (!acc.getName().equals(name) || !acc.getPassword().equals(password)) return "错误: 身份验证失败 / Error: Authentication failed";
        if (acc.getCurrency() != currency) return "错误: 货币类型不匹配 / Error: Currency mismatch";
        if (acc.getBalance() < amount) return "错误: 余额不足 / Error: Insufficient balance";
        
        acc.setBalance(acc.getBalance() - amount);
        return String.format("成功: 取款完成。新余额为: %.2f / Success: Withdrawal completed. New balance: %.2f", acc.getBalance(), acc.getBalance());
    }

    public synchronized String getBalance(int accNum, String password) {
        Account acc = accounts.get(accNum);
        if (acc == null || !acc.getPassword().equals(password)) return "错误: 验证失败 / Error: Authentication failed";
        return String.format("成功: 账户余额为: %.2f %s / Success: Balance is: %.2f %s", acc.getBalance(), acc.getCurrency(), acc.getBalance(), acc.getCurrency());
    }

    public synchronized String transfer(int fromAccNum, String password, int toAccNum, double amount) {
        Account fromAcc = accounts.get(fromAccNum);
        Account toAcc = accounts.get(toAccNum);

        if (fromAcc == null || !fromAcc.getPassword().equals(password)) return "错误: 验证失败 / Error: Authentication failed";
        if (toAcc == null) return "错误: 目标账户不存在 / Error: Target account not found";
        if (fromAcc.getBalance() < amount) return "错误: 余额不足 / Error: Insufficient balance";

        fromAcc.setBalance(fromAcc.getBalance() - amount);
        toAcc.setBalance(toAcc.getBalance() + amount);

        return String.format("成功: 转账完成。已向账号 %d 转入 %.2f %s / Success: Transfer completed. Transferred %.2f %s to account %d", 
                toAccNum, amount, fromAcc.getCurrency(), amount, fromAcc.getCurrency(), toAccNum);
    }

    public Account getAccount(int accNum) { return accounts.get(accNum); }
}