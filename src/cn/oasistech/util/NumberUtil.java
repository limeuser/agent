package cn.oasistech.util;

public final class NumberUtil {
    public static Integer parseInt(String text) {
        try { 
            return Integer.valueOf(text); 
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}
