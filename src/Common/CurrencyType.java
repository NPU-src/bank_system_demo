package Common;

public enum CurrencyType {
    SGD, // 新加坡元
    USD, // 美元
    EUR, // 欧元
    CNY, // 人民币
    HKD; // 港币
    // 根据整数获取枚举，方便在解包（Unmarshalling）时使用
    public static CurrencyType fromInt(int i) {
        if (i < 0 || i >= values().length) return SGD;
        return values()[i];
    }
}