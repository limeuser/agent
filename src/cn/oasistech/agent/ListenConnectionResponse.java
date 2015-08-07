package cn.oasistech.agent;

public class ListenConnectionResponse extends Response {
    public ListenConnectionResponse() {
        super(AgentProtocol.MsgType.ListenConnection, AgentProtocol.Error.Success);
    }
}
