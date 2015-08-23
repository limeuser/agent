package cn.oasistech.agent.server;

import java.util.List;

import mjoys.frame.TLV;

import cn.oasistech.agent.AgentProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class IdFrameDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < AgentProtocol.HeadLength) {
			return;
		}
		
		TLV<ByteBuf> idFrame = new TLV<ByteBuf>();
		idFrame.tag = in.readInt();
		idFrame.length = in.readInt();
		if (in.readableBytes() < idFrame.length) {
			in.resetReaderIndex();
			return;
		}
		idFrame.body = in.readBytes(idFrame.length);
		out.add(idFrame);
	}
}
