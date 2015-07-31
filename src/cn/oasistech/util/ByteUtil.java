package cn.oasistech.util;

public class ByteUtil {
    public static byte[] toLittle(int n) {
        return writeByLittle(new byte[4], 0, n);
    }

    public static byte[] toLittle(short n) {
        return writeByLittle(new byte[2], 0, n);
    }

    public static byte[] toBig(int n) {
        return writeByBig(new byte[4], 0, n);
    }

    public static byte[] toBig(short n) {
        return writeByBig(new byte[2], 0, n);
    }
    
    public static byte[] writeByLittle(byte[] buffer, int offset, short n) {
        buffer[offset + 0] = (byte) (n & 0xff);
        buffer[offset + 1] = (byte) (n >> 8 & 0xff);
        return buffer;
    }
    
    public static byte[] writeByBig(byte[] buffer, int offset, short n) {
        buffer[offset + 1] = (byte) (n & 0xff);
        buffer[offset + 0] = (byte) (n >> 8 & 0xff);
        return buffer;
    }
    
    public static byte[] writeByLittle(byte[] buffer, int offset, int n) {
        buffer[offset + 0] = (byte) (n & 0xff);
        buffer[offset + 1] = (byte) (n >> 8 & 0xff);
        buffer[offset + 2] = (byte) (n >> 16 & 0xff);
        buffer[offset + 3] = (byte) (n >> 24 & 0xff);
        return buffer;
    }
    
    public static byte[] writeByBig(byte[] buffer, int offset, int n) {
        buffer[offset + 3] = (byte) (n & 0xff);
        buffer[offset + 2] = (byte) (n >> 8 & 0xff);
        buffer[offset + 1] = (byte) (n >> 16 & 0xff);
        buffer[offset + 0] = (byte) (n >> 24 & 0xff);
        return buffer;
    }

    public static int bigToInt(byte[] b) {
        return bigToInt(b, 0);
    }
    
    public static int bigToInt(byte[] b, int offset) {
        int s = 0;
        for (int i = 0; i < 3; i++) {
            if (b[offset + i] >= 0) {
                s = s + b[offset + i];
            } else {
                s = s + 256 + b[offset + i];
            }
            s = s * 256;
        }
        if (b[offset + 3] >= 0) {
            s = s + b[offset + 3];
        } else {
            s = s + 256 + b[offset + 3];
        }
        return s;
    }

    public static int littleToInt(byte[] b) {
        return littleToInt(b, 0);
    }
    
    public static int littleToInt(byte[] b, int offset) {
        int s = 0;
        for (int i = 0; i < 3; i++) {
            if (b[offset + 3 - i] >= 0) {
                s = s + b[offset + 3 - i];
            } else {
                s = s + 256 + b[offset + 3 - i];
            }
            s = s * 256;
        }
        if (b[offset + 0] >= 0) {
            s = s + b[offset + 0];
        } else {
            s = s + 256 + b[offset + 0];
        }
        return s;
    }

    public static short bigToShort(byte[] b) {
        return bigToShort(b, 0);
    }
    
    public static short bigToShort(byte[] b, int offset) {
        int s = 0;
        if (b[offset + 0] >= 0) {
            s = s + b[offset + 0];
        } else {
            s = s + 256 + b[offset + 0];
        }
        s = s * 256;
        if (b[offset + 1] >= 0) {
            s = s + b[offset + 1];
        } else {
            s = s + 256 + b[offset + 1];
        }
        short result = (short) s;
        return result;
    }

    public static short littleToShort(byte[] b) {
        return littleToShort(b, 0);
    }
    
    public static short littleToShort(byte[] b, int offset) {
        int s = 0;
        if (b[offset + 1] >= 0) {
            s = s + b[offset + 1];
        } else {
            s = s + 256 + b[offset + 1];
        }
        s = s * 256;
        if (b[offset + 0] >= 0) {
            s = s + b[offset + 0];
        } else {
            s = s + 256 + b[offset + 0];
        }
        short result = (short) s;
        return result;
    }
    
    public final static byte[] copy(byte[] dst, int dstOffset, byte[] src, int srcOffset, int length) {
        System.arraycopy(src, srcOffset, dst, dstOffset, length);
        return dst;
    }
    
    public final static byte[] copy(byte[] dst, int dstOffset, byte[] src) {
        return copy(dst, dstOffset, src, 0, src.length);
    }
}
