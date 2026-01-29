package Common;

public class Marshaller {

    // 1. 封送整数 (int) 为 4 字节
    public static void marshalInt(int value, byte[] buffer, int offset) {
        buffer[offset]     = (byte) (value >> 24);
        buffer[offset + 1] = (byte) (value >> 16);
        buffer[offset + 2] = (byte) (value >> 8);
        buffer[offset + 3] = (byte) value;
    }

    // 2. 拆送字节为整数 (int)
    public static int unmarshalInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xFF) << 24) |
                ((buffer[offset + 1] & 0xFF) << 16) |
                ((buffer[offset + 2] & 0xFF) << 8)  |
                (buffer[offset + 3] & 0xFF);
    }

    // 3. 封送浮点数 (double) 为 8 字节
    // 技巧：先转成 long 的位表示，再按整数方式处理
    public static void marshalDouble(double value, byte[] buffer, int offset) {
        long longValue = Double.doubleToRawLongBits(value);
        for (int i = 7; i >= 0; i--) {
            buffer[offset + i] = (byte) (longValue & 0xFF);
            longValue >>= 8;
        }
    }

    public static double unmarshalDouble(byte[] buffer, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buffer[offset + i] & 0xFF);
        }
        return Double.longBitsToDouble(value);
    }

    // 4. 封送变长字符串 (String)
    // 协议格式：[4字节长度] + [实际内容字节]
    public static int marshalString(String str, byte[] buffer, int offset) {
        byte[] strBytes = str.getBytes();
        int len = strBytes.length;
        marshalInt(len, buffer, offset); // 先存入长度 [cite: 83]
        System.arraycopy(strBytes, 0, buffer, offset + 4, len);
        return 4 + len; // 返回总共占用的长度，方便计算下一个字段的 offset
    }

    public static String unmarshalString(byte[] buffer, int offset) {
        int length = unmarshalInt(buffer, offset);
        return new String(buffer, offset + 4, length);
    }

    public static int getStringEncodedLength(String str) {
        return 4 + str.getBytes().length;
    }
}