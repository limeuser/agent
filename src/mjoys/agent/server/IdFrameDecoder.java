package mjoys.agent.server;

import java.util.List;

import mjoys.agent.Agent;
import mjoys.frame.TLV;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class IdFrameDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < Agent.HeadLength) {
			return;
		}
		
		int id = in.readInt();
		int bodyLength = in.readInt();
		if (in.readableBytes() < bodyLength) {
			in.resetReaderIndex();
			return;
		}
		in.resetReaderIndex();
		int readableBytes = in.readableBytes();
		TLV<ByteBuf> frame = new TLV<ByteBuf>();
		frame.tag = id;
		frame.length = bodyLength;
		frame.body = Unpooled.buffer(Agent.HeadLength + bodyLength);
		in.readBytes(frame.body);
		int frameBodyLength = in.readableBytes();
		frame.body.skipBytes(Agent.HeadLength);
		out.add(frame);
		System.out.println(String.format("decode a message: readablebytes:%d:%d", readableBytes, frameBodyLength));
	}
}
