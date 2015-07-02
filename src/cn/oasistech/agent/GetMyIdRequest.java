package cn.oasistech.agent;

public class GetMyIdRequest extends Request {
    public GetMyIdRequest() {
        super(AgentProtocol.MsgType.GetMyId);
    }
    
    @Override 
    public String toString() {
        return super.toString();
    }
}
