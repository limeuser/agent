package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;

public class GetTagRequest extends Request {
    private List<IdKey> idKeys = new ArrayList<IdKey>();
    
    public GetTagRequest() {
        super(AgentProtocol.MsgType.GetTag);
    }

    public List<IdKey> getIdKeys() {
        return idKeys;
    }

    public void setIdKeys(List<IdKey> idKeys) {
        this.idKeys = idKeys;
    }
    
    @Override 
    public String toString() {
        return super.toString() + ", " + Formater.formatCollection(idKeys);
    }
}