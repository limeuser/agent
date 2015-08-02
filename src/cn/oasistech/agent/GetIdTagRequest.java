package cn.oasistech.agent;

public class GetIdTagRequest extends GetIdRequest {
    public GetIdTagRequest() {
    	super();
        super.setType(AgentProtocol.MsgType.GetIdTag.name());
    }
}
