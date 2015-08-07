package cn.oasistech.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

public class Tag {
    private String key;
    private String value;
    
    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public Tag() {
        this.key = "";
        this.value = "";
    }
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    public static final Map<String, String> toMap(Collection<Tag> tags) {
        Map<String, String> map = new HashMap<String, String>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }
    
    @Override
    public String toString() {
        return key + ":" + value;
    }
}
