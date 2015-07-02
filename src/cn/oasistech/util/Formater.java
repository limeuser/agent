package cn.oasistech.util;

import java.util.Collection;

public class Formater {
    public static String formatMac(byte[] mac) {
        return String.format("%02X-%02X-%02X-%02X-%02X-%02X", 
                mac[0],mac[1],mac[2],mac[3],mac[4],mac[5]);
    }
    public static String formatBytes(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            str.append(String.format("%02X-", bytes[i]));
        }
        str.setLength(str.length() - 1);
        return str.toString();
    }
    
    public static String formatCollection(Collection<?> es) {
        return formatArray(es.toArray());
    }
    
    public static <T> String formatArray(T[] es) {
        StringBuilder str = new StringBuilder();
        boolean isEmpty = true;
        for (Object e : es) {
            str.append(e.toString()).append(", ");
            isEmpty = false;
        }
        if (!isEmpty) {
            str.setLength(str.length() - ", ".length());
        }
        return str.toString();
    }
    
    public static String formatEntry(Object key, Object value) {
        return key + ":" + value;
    }
    
    public static String formatEntries(Object ...es) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < es.length - 1; i+=2) {
            str.append(formatEntry(es[i], es[i + 1])).append(", ");
        }
        if (es.length > 0) {
            str.setLength(str.length() - ", ".length());
        }
        return str.toString();
    }
    
    public static String format(Object ...es) {
        return formatArray(es);
    }
}