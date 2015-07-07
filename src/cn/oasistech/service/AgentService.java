package cn.oasistech.service;

import cn.oasistech.util.Address;

public interface AgentService {
    public boolean start(Address agentServer);
    public void stop();
}
