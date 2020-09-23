package blockchain.utils;

import java.util.Arrays;

public class ArrayUtils {
    public static byte[] addAll(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b.clone();
        }
        if (b == null) {
            return a.clone();
        }
        byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] addAll(byte[][] bytes) {
        byte[] result = new byte[sumLengths(bytes)];
        int offset = 0;
        for (byte[] subArray :
                bytes) {
            System.arraycopy(subArray, 0, result, offset, subArray.length);
            offset += subArray.length;
        }
        return result;
    }

    private static int sumLengths(byte[][] items) {
        int length = 0;
        for (byte[] item :
                items) {
            length += item.length;
        }
        return length;
    }
}
