package mjoys.agent.client;

import java.nio.ByteBuffer;

import mjoys.agent.Agent;
import mjoys.agent.Response;
import mjoys.agent.util.Cfg;
import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.Serializer;
import mjoys.util.Logger;

public class TestAgentRpcHandler implements AgentRpcHandler<ByteBuffer> {
    private Serializer serializer;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public TestAgentRpcHandler() {
        try {
            this.serializer = (Serializer)Class.forName(Cfg.getSerializerClassName()).newInstance();
        } catch (Exception e) {
            logger.log("can't create parser class:", e);
        }
    }
    
    @Override
    public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> idFrame) {
        if (idFrame.tag == Agent.PublicService.Agent.id) {
        	TV<ByteBuffer> responseFrame = ByteBufferParser.parseTV(idFrame.body);
        	
        	Agent.MsgType type = Agent.getMsgType(responseFrame.tag);
        	if (type == Agent.MsgType.Unknown) {
        		logger.log("response msg type is unknown");
        		return;
        	}
        	
        	Response response = Agent.decodeAgentResponse(type, new ByteBufferInputStream(responseFrame.body), serializer);
        	if (response == null) {
        		logger.log("parse response error");
        		return;
        	}
        	
            logger.log("asyn rpc recv response:%s", response.toString());
        }
    }
}