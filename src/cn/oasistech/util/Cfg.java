package cn.oasistech.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Cfg {
    public enum Key {
        serveraddr,
        parserclass
    }
    
    public static String getRoot() {
        return System.getProperty("user.dir");
    }
    
    public static String getCfgFilePath() {
        return getRoot() + System.getProperty("file.separator") + "sh"; 
    }
    
    public static File getCfgFile(String cfgFile) {
        return new File(getCfgFilePath() + System.getProperty("file.separator") + cfgFile);
    }
    
    private final static String AgentCfgFileName = "agent.cfg";
    
    public final static Properties getProperties(File file) {
        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            Properties p = new Properties();   
            p.load(in);
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return new Properties();
        }   
    }
    
    public final static Properties getAgentCfg() {
        return getProperties(getCfgFile(AgentCfgFileName));
    }
    
    public final static String getServerAddress() {
        Properties p = Cfg.getAgentCfg();
        return p.getProperty(Cfg.Key.serveraddr.name()).trim();
    }
    
    public final static String getParserClassName() {
        Properties p = Cfg.getAgentCfg();
        return p.getProperty(Cfg.Key.parserclass.name()).trim();
    }
}
