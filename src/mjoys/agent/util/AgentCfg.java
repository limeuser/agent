package mjoys.agent.util;

import java.util.Properties;

public class AgentCfg extends mjoys.util.Cfg {
    private AgentCfg(String cfgFilePathInRoot, String defaultPropertyFileName) {
        super(cfgFilePathInRoot, defaultPropertyFileName);
    }
    
    public final static AgentCfg instance = new AgentCfg("cfg", "agent.cfg");
    
    public enum Key {
        serveraddress,
        serializerclass
    }
    
    public String getServerAddress() {
        Properties p = instance.getDefaultPropertyCfg();
        return p.getProperty(AgentCfg.Key.serveraddress.name()).trim();
    }
    
    public String getSerializerClassName() {
        Properties p = instance.getDefaultPropertyCfg();
        return p.getProperty(AgentCfg.Key.serializerclass.name()).trim();
    }
}