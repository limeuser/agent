package cn.oasistech.service;

import java.io.UnsupportedEncodingException;

import cn.oasistech.agent.AgentJsonParser;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.util.AsynClient;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.Tag;

public class Text {
    private AgentAsynRpc agentAsynRpc;
    private AsynClient asynClient;
    //private AgentSyncRpc agentSyncRpc;
    
    public void start() {
        asynClient = new AsynClient(new MonitorHandler());
        asynClient.start(Cfg.getServerAddress());
        agentAsynRpc = new AgentAsynRpc(asynClient, AgentJsonParser.instance);
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "monitor"));
    }
    
    public void sendTo(int id, String text) {
        try {
            agentAsynRpc.sendTo(id, text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    public void sendTo(String service, String text) {
        agentAsynRpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), service));
    }
}