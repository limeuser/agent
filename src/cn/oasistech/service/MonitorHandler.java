package cn.oasistech.service;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.util.ClientHandler;
import cn.oasistech.util.Logger;
import cn.oasistech.util.StringUtil;

public class MonitorHandler implements ClientHandler {
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    @Override
    public void handle(byte[] buffer, int offset, int length) {
        IdFrame frame = AgentProtocol.parseIdFrame(buffer);
        if (frame.getId() != AgentProtocol.PublicService.Agent.id) {
            String text = StringUtil.getUTF8String(frame.getBody());
            
            logger.log(text);
        }
    }
}
