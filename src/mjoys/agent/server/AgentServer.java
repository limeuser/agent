package mjoys.agent.server;

import mjoys.util.Address;

public interface AgentServer {
    public boolean start(Address address);
    public void stop();
}
