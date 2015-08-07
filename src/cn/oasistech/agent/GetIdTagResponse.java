package cn.oasistech.agent;

public class GetIdTagResponse extends GetTagResponse {
	public GetIdTagResponse() {
		super();
		super.setType(AgentProtocol.MsgType.GetTag);
	}
}
