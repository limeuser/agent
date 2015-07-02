package cn.oasistech.service;

import java.util.List;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.util.Tag;

public class ClusterManager {
    private List<AgentSyncRpc> agentSyncRpcs;
    
    public void send
    public void sendTo(String service, byte[] body) {
        for (AgentSyncRpc rpc : agentSyncRpcs) {
            GetIdResponse response = rpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), service));
            for (int id : response.getIds()) {
               rpc.sendTo(id, body); 
            }
        }
    }
}
