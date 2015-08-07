package cn.oasistech.agent;

public class ListenConnectionRequest extends GetIdRequest {
    public ListenConnectionRequest() {
        super();
        super.setType(AgentProtocol.MsgType.ListenConnection);
    }
}