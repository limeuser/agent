package cn.oasistech.agent.server;

import java.util.List;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class IdFrameDecoder extends ByteToMessageDecoder{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> result) throws Exception {
        // | id:4 | data-length:4 | data:data-length |
        if (buf.readableBytes() < AgentProtocol.IdHeadLength) {
            return;
        }
        
        int id = buf.readInt();
        int length = buf.readInt();
        
        if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return;
        }

        IdFrame frame = new IdFrame();
        frame.setId(id);
        frame.setBodyLength(length);
        
        byte[] body = new byte[length];
        buf.readBytes(body);
        frame.setBody(body);
        
        result.add(frame);
    }
}
