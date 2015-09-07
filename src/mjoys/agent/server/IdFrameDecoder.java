package mjoys.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import mjoys.agent.Agent;
import mjoys.frame.TLV;

public class IdFrameDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < Agent.HeadLength) {
			return;
		}
		
		int id = in.getInt(0);
		int bodyLength = in.getInt(4);
		int frameLength = Agent.HeadLength + bodyLength;
		if (in.readableBytes() < frameLength) {
			return;
		}
		TLV<ByteBuf> frame = new TLV<ByteBuf>();
		frame.tag = id;
		frame.length = bodyLength;
		frame.body = Unpooled.buffer(frameLength);
		int before = in.readableBytes();
		in.readBytes(frame.body);
		int after = in.readableBytes();
		frame.body.skipBytes(Agent.HeadLength);
		out.add(frame);
		System.out.println(String.format("decode message%d:%d, id=%d,length=%d", before, after, id, bodyLength));
	}
}
