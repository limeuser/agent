package cn.oasistech.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;
import cn.oasistech.util.Address;
import cn.oasistech.util.Logger;
import cn.oasistech.util.StringUtil;
import cn.oasistech.util.Tag;

public class Shell implements AgentService {
    private AgentAsynRpc agentAsynRpc;
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    @Override
    public boolean start(Address agentServer) {
        agentAsynRpc = new AgentAsynRpc();
        
        if (false == agentAsynRpc.start(agentServer, new CmdHandler())) {
            return false;
        }
        
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "shell"));
        
        return true;
    }
    
    @Override
    public void stop() {
        agentAsynRpc.stop();
    }
    
    class CmdHandler implements AgentRpcHandler {
        @Override
        public void handle(AgentAsynRpc rpc, IdFrame frame) {
            if (frame.getId() != AgentProtocol.PublicService.Agent.id) {
                handleCmd(rpc, frame);
            } else {
                logger.log("agent response: %s", rpc.getParser().decodeResponse(frame.getBody()).toString());
            }
        }
        
        private void handleCmd(AgentAsynRpc rpc, IdFrame frame) {
            try {
                String cmd = new String(frame.getBody(), "UTF-8");
                logger.log("run cmd: %s", cmd);
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                StringBuilder str = new StringBuilder();
                while (line != null) {
                    str.append(line).append("\r\n");
                    line = reader.readLine();
                }
                logger.log("cmd result:%s", str.toString());
                rpc.sendTo(frame.getId(), StringUtil.toBytes(str.toString(), "UTF-8"));
            } catch (Exception e) {
                logger.log("shell run cmd exception:", e);
                rpc.sendTo(frame.getId(), StringUtil.toBytes(e.toString(), "UTF-8"));
            }
        }
    }
}
