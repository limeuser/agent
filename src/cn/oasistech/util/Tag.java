package cn.oasistech.util;

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
    
    @Override
    public String toString() {
        return key + ":" + value;
    }
}
