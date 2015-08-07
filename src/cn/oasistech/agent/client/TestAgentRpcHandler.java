package cn.oasistech.agent.client;

import mjoys.util.Logger;
import cn.oasistech.agent.AgentMsgSerializer;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.Response;
import cn.oasistech.util.Cfg;

public class TestAgentRpcHandler implements AgentRpcHandler {
    private AgentMsgSerializer parser;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public TestAgentRpcHandler() {
        try {
            this.parser = (AgentMsgSerializer)Class.forName(Cfg.getParserClassName()).newInstance();
        } catch (Exception e) {
            logger.log("can't create parser class:", e);
        }
    }
    
    @Override
    public void handle(AgentAsynRpc rpc, IdFrame frame) {
        if (frame.getId() == AgentProtocol.PublicService.Agent.id) {
            Response response = parser.decodeResponse(frame.getBody());
            logger.log("asyn rpc recv response:%s", response.toString());
        }
    }
}