package cn.oasistech.util;

import java.util.Properties;

public class Cfg extends mjoys.util.Cfg {
    private Cfg(String cfgFilePathInRoot, String defaultPropertyFileName) {
        super(cfgFilePathInRoot, defaultPropertyFileName);
    }
    
    private final static Cfg instance = new Cfg("sh", "agent.cfg");
    
    public enum Key {
        serveraddress,
        parserclass
    }
    
    public final static String getServerAddress() {
        Properties p = instance.getDefaultPropertyCfg();
        return p.getProperty(Cfg.Key.serveraddress.name()).trim();
    }
    
    public final static String getParserClassName() {
        Properties p = instance.getDefaultPropertyCfg();
        return p.getProperty(Cfg.Key.parserclass.name()).trim();
    }
}
