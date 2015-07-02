package cn.oasistech.agent.client;

import cn.oasistech.agent.AgentParser;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.Response;
import cn.oasistech.util.ClientHandler;
import cn.oasistech.util.Logger;

public class AgentClientHandler implements ClientHandler {
    private AgentParser parser;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public AgentClientHandler(AgentParser parser) {
        this.parser = parser;
    }
    
    @Override
    public void handle(byte[] buffer, int offset, int length) {
        IdFrame frame = AgentProtocol.parseIdFrame(buffer);
        if (frame.getId() == AgentProtocol.PublicService.Agent.id) {
            Response response = parser.decodeResponse(frame.getBody());
            logger.log("asyn rpc recv response:%s", response.toString());
        }
    }
}