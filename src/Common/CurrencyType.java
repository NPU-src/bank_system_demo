package Common;

public enum CurrencyType {
    SGD, // 新加坡元 / Singapore Dollar
    USD, // 美元 / US Dollar
    EUR, // 欧元 / Euro
    CNY, // 人民币 / Chinese Yuan
    HKD; // 港币 / Hong Kong Dollar
    // 根据整数获取枚举，方便在解包（Unmarshalling）时使用
    // Get Enum from integer, convenient for Unmarshalling
    public static CurrencyType fromInt(int i) {
        if (i < 0 || i >= values().length) return SGD;
        return values()[i];
    }
}