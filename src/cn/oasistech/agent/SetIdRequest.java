package cn.oasistech.agent;

import cn.oasistech.util.Formater;


public class SetIdRequest extends Request {
    private int id;
    
    public SetIdRequest() {
        super(AgentProtocol.MsgType.SetId);
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    @Override 
    public String toString() {
        return super.toString() + Formater.formatEntry("id", id);
    }
}
