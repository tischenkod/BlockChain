package blockchain.utils;

public class Longs {
    public static byte[] toByteArray(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0 ; i--) {
            result[i] = (byte)((int)(l & 255L));
            l >>= 8;
        }
        return new byte[0];
    }
}
