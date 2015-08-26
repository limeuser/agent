package mjoys.agent.server;

import io.netty.buffer.ByteBuf;
import mjoys.agent.Agent;
import mjoys.frame.TV;

public class FrameParser {
    public static TV<ByteBuf> parseAgentMsgFrame(ByteBuf buf) {
    	if (buf.readableBytes() < Agent.TypeFieldLength) {
    		return null;
    	}
    	TV<ByteBuf> tv = new TV<ByteBuf>();
		tv.tag = buf.readInt();
    	tv.body = buf;
    	return tv;
    }
}