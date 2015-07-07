package cn.oasistech.service;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;
import cn.oasistech.util.Address;
import cn.oasistech.util.Logger;
import cn.oasistech.util.StringUtil;
import cn.oasistech.util.Tag;

public class Text implements AgentService {
    private AgentAsynRpc agentAsynRpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    @Override
    public boolean start(Address address) {
        agentAsynRpc = new AgentAsynRpc();
        
        if (agentAsynRpc.start(address, new TextHandler()) == false) {
            return false;
        }
        
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "text"));
        
        return true;
    }
    
    @Override
    public void stop() {
        agentAsynRpc.stop();
    }
    
    public class TextHandler implements AgentRpcHandler {
        @Override
        public void handle(AgentAsynRpc rpc, IdFrame frame) {
            if (frame.getId() != AgentProtocol.PublicService.Agent.id) {
                String text = StringUtil.getUTF8String(frame.getBody());
                logger.log(text);
            } else {
                logger.log("agent response: %s", rpc.getParser().decodeResponse(frame.getBody()).toString());
            }
        }
    }
}