package cn.oasistech.agent;

public class Request {
    protected String type;

    public Request() {
        
    }
    
    public Request(AgentProtocol.MsgType type) {
        this.type = type.name();
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type;
    }
}
