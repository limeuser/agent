package cn.oasistech.service;

import cn.oasistech.agent.AgentJsonParser;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.util.AsynClient;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.Tag;

public class Monitor {
    private AsynClient asynClient;
    private AgentAsynRpc agentAsynRpc;
    
    public void start() {
        asynClient = new AsynClient(new MonitorHandler());
        asynClient.start(Cfg.getServerAddress());
        agentAsynRpc = new AgentAsynRpc(asynClient, AgentJsonParser.instance);
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "monitor"));
    }
}
