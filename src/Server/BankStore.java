package Server;

import Common.Account;
import Common.CurrencyType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BankStore {
    // 存储所有账户信息
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger accountNumberCounter = new AtomicInteger(1000);

    public synchronized int openAccount(String name, String password, CurrencyType currency, double balance) {
        int accNum = accountNumberCounter.getAndIncrement();
        accounts.put(accNum, new Account(accNum, name, password, currency, balance));
        return accNum;
    }

    public synchronized String closeAccount(int accNum, String name, String password) {
        Account acc = accounts.get(accNum);
        if (acc == null) return "错误: 账户不存在";
        if (!acc.getName().equals(name) || !acc.getPassword().equals(password)) {
            return "错误: 身份验证失败";
        }
        accounts.remove(accNum);
        return "成功: 账户已关闭";
    }

    public synchronized String deposit(int accNum, String password, double amount) {
        Account acc = accounts.get(accNum);
        if (acc == null || !acc.getPassword().equals(password)) return "错误: 验证失败";
        acc.setBalance(acc.getBalance() + amount);
        return String.format("成功: 存款完成。新余额为: %.2f", acc.getBalance());
    }

    public synchronized String withdraw(int accNum, String password, double amount) {
        Account acc = accounts.get(accNum);
        if (acc == null || !acc.getPassword().equals(password)) return "错误: 验证失败";
        if (acc.getBalance() < amount) return "错误: 余额不足";
        acc.setBalance(acc.getBalance() - amount);
        return String.format("成功: 取款完成。新余额为: %.2f", acc.getBalance());
    }

    public synchronized String getBalance(int accNum, String password) {
        Account acc = accounts.get(accNum);
        if (acc == null || !acc.getPassword().equals(password)) return "错误: 验证失败";
        return String.format("成功: 账户余额为: %.2f %s", acc.getBalance(), acc.getCurrency());
    }

    public synchronized String transfer(int fromAccNum, String password, int toAccNum, double amount) {
        Account fromAcc = accounts.get(fromAccNum);
        Account toAcc = accounts.get(toAccNum);

        if (fromAcc == null || !fromAcc.getPassword().equals(password)) return "错误: 验证失败";
        if (toAcc == null) return "错误: 目标账户不存在";
        if (fromAcc.getBalance() < amount) return "错误: 余额不足";

        fromAcc.setBalance(fromAcc.getBalance() - amount);
        toAcc.setBalance(toAcc.getBalance() + amount);

        return String.format("成功: 转账完成。已向账号 %d 转入 %.2f %s", toAccNum, amount, fromAcc.getCurrency());
    }

    public Account getAccount(int accNum) { return accounts.get(accNum); }
}