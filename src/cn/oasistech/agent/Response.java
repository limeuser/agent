package cn.oasistech.agent;

import cn.oasistech.util.Formater;

public class Response {
    protected String type;
    protected String error;
    
    public Response() {
        
    }
    
    public Response(AgentProtocol.MsgType type, AgentProtocol.Error error) {
        this.type = type.name();
        this.error = error.name();
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return Formater.formatEntries("type", type, "error", error);
    }
}
