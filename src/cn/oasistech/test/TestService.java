package cn.oasistech.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.util.Address;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.Logger;
import cn.oasistech.util.StringUtil;
import cn.oasistech.util.Tag;

public class TestService {
    //private static Server agentServer;
    private AgentSyncRpc syncRpc = new AgentSyncRpc();
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    @Before
    public void setUp() {
        Address address = Address.parse(Cfg.getServerAddress());
        //agentServer.start(address);
        syncRpc.start(address);
    }
    
    @After
    public void after() {
        syncRpc.stop();
        //agentServer.stop();
    }
    
    @Test
    public void testShell() {
        GetIdResponse response = syncRpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), "shell"));
        Assert.assertNotNull(response);
        
        for (int id : response.getIds()) {
            IdFrame frame = syncRpc.call(id, StringUtil.toBytes("cmd /c dir", "UTF-8"));
            String echo = StringUtil.getUTF8String(frame.getBody());
            logger.log(echo);
        }   
    }
    
    @Test
    public void testText() {
        GetIdResponse response = syncRpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), "text"));
        Assert.assertNotNull(response);
        for (int id : response.getIds()) {
            syncRpc.sendTo(id, StringUtil.toBytes("hello", "UTF-8"));
        }
    }
    
    @Test
    public void testEcho() {
        GetIdResponse response = syncRpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), "echo"));
        Assert.assertNotNull(response);
        for (int id : response.getIds()) {
            IdFrame frame = syncRpc.call(id, StringUtil.toBytes("hello", "UTF-8"));
            String echo = StringUtil.getUTF8String(frame.getBody());
            Assert.assertEquals(echo, "hello");
            logger.log("response: " + echo);
        }
    }
}
