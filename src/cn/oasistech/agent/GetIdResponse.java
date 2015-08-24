package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;

public class GetIdResponse extends Response {
    private List<Integer> ids = new ArrayList<Integer>();

    public List<Integer> getIds() {
        return ids;
    }
    public void setIds(List<Integer> id) {
        this.ids = id;
    }
    
    @Override 
    public String toString() {
        return Formater.format(super.toString(), Formater.formatEntry("ids", Formater.formatCollection(ids)));
    }
}
