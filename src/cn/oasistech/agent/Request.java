package cn.oasistech.agent;

public class Request {
    protected AgentProtocol.MsgType type;

    public Request() {
        
    }
    
    public Request(AgentProtocol.MsgType type) {
        this.type = type;
    }
    
    public AgentProtocol.MsgType getType() {
        return type;
    }

    public void setType(AgentProtocol.MsgType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type.name();
    }
}
