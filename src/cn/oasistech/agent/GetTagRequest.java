package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;

public class GetTagRequest {
    private List<IdKey> idKeys = new ArrayList<IdKey>();

    public List<IdKey> getIdKeys() {
        return idKeys;
    }

    public void setIdKeys(List<IdKey> idKeys) {
        this.idKeys = idKeys;
    }
    
    @Override 
    public String toString() {
        return Formater.formatCollection(idKeys);
    }
}