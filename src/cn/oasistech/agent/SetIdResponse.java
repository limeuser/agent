package cn.oasistech.agent;

public class SetIdResponse extends Response {
    public SetIdResponse() {
        super(AgentProtocol.MsgType.SetId, AgentProtocol.Error.Success);
    }
    
    @Override 
    public String toString() {
        return super.toString();
    }
}
