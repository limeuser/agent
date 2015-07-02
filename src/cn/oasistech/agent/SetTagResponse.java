package cn.oasistech.agent;

public class SetTagResponse extends Response {
    public SetTagResponse() {
        super(AgentProtocol.MsgType.SetTag, AgentProtocol.Error.Success);
    }
    
    @Override 
    public String toString() {
        return super.toString();
    }
}