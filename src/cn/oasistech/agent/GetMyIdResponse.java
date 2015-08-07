package cn.oasistech.agent;

import mjoys.util.Formater;

public class GetMyIdResponse extends Response {
    private int id;

    public GetMyIdResponse() {
        super(AgentProtocol.MsgType.GetMyId, AgentProtocol.Error.Success);
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @Override 
    public String toString() {
        return super.toString() + Formater.formatEntries("id", id);
    }
}
