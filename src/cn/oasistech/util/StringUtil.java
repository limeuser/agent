package cn.oasistech.util;

import java.io.UnsupportedEncodingException;

public class StringUtil {
    public static byte[] toBytes(String str, String code) {
        try {
            return str.getBytes(code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getString(byte[] bytes, String code) {
        try {
            return new String(bytes, code);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static String getUTF8String(byte[] bytes) {
        return getString(bytes, "UTF-8");
    }
}
