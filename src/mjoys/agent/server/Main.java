package mjoys.agent.server;

import mjoys.agent.util.Cfg;
import mjoys.util.Address;

public class Main {
    private static AgentNettyServer agentServer;
    
    public static void main(String[] args) {
        agentServer = new AgentNettyServer();
        if (false == agentServer.start(Address.parse(Cfg.getServerAddress()))) {
            return;
        }
    }
}
