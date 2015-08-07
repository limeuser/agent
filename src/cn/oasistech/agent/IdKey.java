package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;

public class IdKey {
    private int id;
    private List<String> keys = new ArrayList<String>();
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public List<String> getKeys() {
        return keys;
    }
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
    
    @Override
    public String toString() {
        return Formater.formatEntries("id", id, "keys", Formater.formatCollection(keys));
    }
}
