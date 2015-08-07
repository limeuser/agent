package cn.oasistech.agent.server;

import mjoys.util.Address;
import cn.oasistech.util.Cfg;

public class Main {
    private static AgentNettyServer agentServer;
    
    public static void main(String[] args) {
        agentServer = new AgentNettyServer();
        if (false == agentServer.start(Address.parse(Cfg.getServerAddress()))) {
            return;
        }
    }
}
