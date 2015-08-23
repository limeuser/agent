package cn.oasistech.agent.client;

import mjoys.frame.TLV;

public interface AgentRpcHandler<T> {
	void handle(AgentAsynRpc rpc, TLV<T> frame);
}