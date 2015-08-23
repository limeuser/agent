package cn.oasistech.agent.server;

import io.netty.buffer.ByteBuf;
import mjoys.frame.TV;
import cn.oasistech.agent.AgentProtocol;

public class FrameParser {
    public static TV<ByteBuf> parseAgentMsgFrame(ByteBuf buf) {
    	if (buf.readableBytes() < AgentProtocol.TypeFieldLength) {
    		return null;
    	}
    	TV<ByteBuf> tv = new TV<ByteBuf>();
		tv.tag = buf.readInt();
    	tv.body = buf;
    	return tv;
    }
}