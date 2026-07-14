package dev.redgamer6427a.core.messagebus;

import java.nio.charset.StandardCharsets;

public class MessageBusUtil {

    public static byte[] toFixedField(String s, int len) {
        byte[] raw = s.getBytes(StandardCharsets.US_ASCII);
        if (raw.length > len) throw new IllegalArgumentException("Field too long: " + s);
        byte[] out = new byte[len];
        System.arraycopy(raw, 0, out, 0, raw.length); // rest stays zero-padded
        return out;
    }

    public static String fromFixedField(byte[] field) {
        int end = 0;
        while (end < field.length && field[end] != 0) end++; // stop at first null byte
        return new String(field, 0, end, StandardCharsets.US_ASCII);
    }

    private MessageBusUtil() {}

}
