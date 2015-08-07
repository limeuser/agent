package cn.oasistech.agent;

import mjoys.util.Formater;

public class Response {
    protected AgentProtocol.MsgType type;
    protected AgentProtocol.Error error;
    
    public Response() {
        
    }
    
    public Response(AgentProtocol.MsgType type, AgentProtocol.Error error) {
        this.type = type;
        this.error = error;
    }
    
    public AgentProtocol.MsgType getType() {
        return type;
    }
    public void setType(AgentProtocol.MsgType type) {
        this.type = type;
    }
    public AgentProtocol.Error getError() {
        return error;
    }
    public void setError(AgentProtocol.Error error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return Formater.formatEntries("type", type.name(), "error", error.name());
    }
}
