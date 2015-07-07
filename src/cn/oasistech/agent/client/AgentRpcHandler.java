package cn.oasistech.agent.client;

import cn.oasistech.agent.IdFrame;

public interface AgentRpcHandler {
    void handle(AgentAsynRpc rpc, IdFrame idFrame);
}