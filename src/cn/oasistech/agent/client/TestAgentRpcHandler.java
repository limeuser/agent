package cn.oasistech.agent.client;

import java.nio.ByteBuffer;

import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.Serializer;
import mjoys.util.Logger;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.Response;
import cn.oasistech.util.Cfg;

public class TestAgentRpcHandler implements AgentRpcHandler<ByteBuffer> {
    private Serializer serializer;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public TestAgentRpcHandler() {
        try {
            this.serializer = (Serializer)Class.forName(Cfg.getParserClassName()).newInstance();
        } catch (Exception e) {
            logger.log("can't create parser class:", e);
        }
    }
    
    @Override
    public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> idFrame) {
        if (idFrame.tag == AgentProtocol.PublicService.Agent.id) {
        	TV<ByteBuffer> responseFrame = ByteBufferParser.parseTV(idFrame.body);
        	
        	AgentProtocol.MsgType type = AgentProtocol.getMsgType(responseFrame.tag);
        	if (type == AgentProtocol.MsgType.Unknown) {
        		logger.log("response msg type is unknown");
        		return;
        	}
        	
        	Response response = AgentProtocol.decodeAgentResponse(type, new ByteBufferInputStream(responseFrame.body), serializer);
        	if (response == null) {
        		logger.log("parse response error");
        		return;
        	}
        	
            logger.log("asyn rpc recv response:%s", response.toString());
        }
    }
}