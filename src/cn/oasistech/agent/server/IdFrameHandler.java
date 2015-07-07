package cn.oasistech.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.Peer;
import cn.oasistech.agent.Response;
import cn.oasistech.util.Logger;

public class IdFrameHandler extends ChannelInboundHandlerAdapter {
    private AgentContext<Channel> agentCtx;
    private AgentHandler<Channel> handler;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public IdFrameHandler(AgentContext<Channel> agentCtx, AgentHandler<Channel> handler) {
        this.agentCtx = agentCtx;
        this.handler = handler;
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        if (agentCtx.getChannelMap().get(ctx.channel()) == null) {
            Peer<Channel> peer = addNewPeer(ctx.channel());
            logger.log("add new peer when channel active:%s", peer.toString());
        }
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        Peer<Channel> peer = agentCtx.getChannelMap().get(ctx.channel());
        if (peer == null) {
            logger.log("can't find peer when channelInactive");
        } else {
            removeDisconnectedPeer(peer);
            logger.log("remove peer when channelInactive:%s", peer.toString());
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Peer<Channel> peer = agentCtx.getChannelMap().get(ctx.channel());
    
            // a new peer connected
            if (peer == null) {
                peer = addNewPeer(ctx.channel());
                logger.log("add new peer when channelRead:%s", peer.toString());
            }

            IdFrame idFrame = (IdFrame) msg;
            if (idFrame.getId() == AgentProtocol.PublicService.Agent.id) {
                byte[] response = handler.processRequest(peer, idFrame.getBody());
                ByteBuf writeBuffer = Unpooled.buffer();
                writeBuffer.writeInt(idFrame.getId());
                writeBuffer.writeInt(response.length);
                writeBuffer.writeBytes(response);
                ctx.channel().writeAndFlush(writeBuffer);
            } else {
                route(peer, idFrame);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
    
    private void setPublicTag(Peer<Channel> peer) {
        InetSocketAddress address = (InetSocketAddress) peer.getChannel().remoteAddress();
        peer.getTags().put(AgentProtocol.PublicTag.id.name(), String.valueOf(peer.getId()));
        peer.getTags().put(AgentProtocol.PublicTag.address.name(), address.getAddress().getHostAddress());
        peer.getTags().put(AgentProtocol.PublicTag.name.name(), address.getAddress().getHostName());
        peer.getTags().put(AgentProtocol.PublicTag.port.name(), String.valueOf(address.getPort()));
        
        logger.log("new peer: id=%d, name=%s, address=%s, port=%d", 
                peer.getId(), 
                address.getAddress().getHostName(), 
                address.getAddress().getHostAddress(), 
                address.getPort());
    }

    private void route(Peer<Channel> srcHost, IdFrame idFrame) {
        // route request: replace dst-id with src-id in frame
        int dstId = idFrame.getId();
        int srcId = srcHost.getId();

        Peer<Channel> dstHost = agentCtx.getIdMap().get(dstId);
        if (dstHost == null) {
            logger.log("can't find router connection destid=%d", dstId);
            sendError(srcHost.getChannel(), AgentProtocol.MsgType.Route, AgentProtocol.Error.NoConnection);
            return;
        }

        ByteBuf writeBuffer = Unpooled.buffer();
        writeBuffer.writeInt(srcId);
        writeBuffer.writeInt(idFrame.getBodyLength());
        writeBuffer.writeBytes(idFrame.getBody());
        dstHost.getChannel().writeAndFlush(writeBuffer);
        logger.log("route a message from %s to %s", srcHost.toString(), dstHost.toString());
    }

    private void sendError(Channel channel, AgentProtocol.MsgType type, AgentProtocol.Error error) {
        Response response = new Response();
        response.setType(type.name());
        response.setError(error.name());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        AgentProtocol.write(buffer, handler.getAgentParser().encodeResponse(response));
        channel.writeAndFlush(buffer.toByteArray());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isWritable()) {
            sendError(ctx.channel(), AgentProtocol.MsgType.Unknown, AgentProtocol.Error.InternalError);
        }

        ctx.close().syncUninterruptibly();
        
        logger.log("process connection(%s) exception: %s, close connection", cause, ctx.channel().remoteAddress().toString(), cause.toString());
        
        Peer<Channel> peer = agentCtx.getChannelMap().get(ctx.channel());
        if (peer == null) {
            logger.log("can't find peer when exception");
        } else {
            removeDisconnectedPeer(peer);
            logger.log("remove peer when exception:%s", peer.toString());
        }
    }
    
    private Peer<Channel> addNewPeer(Channel channel) {
        int id = agentCtx.getIdGenerator().getId();
        Peer<Channel> peer = new Peer<Channel>(id, channel);
        setPublicTag(peer);
        agentCtx.getChannelMap().put(channel, peer);
        agentCtx.getIdMap().put(id, peer);
        return peer;
    }
    
    private void removeDisconnectedPeer(Peer<Channel> peer) {
        agentCtx.getIdMap().remove(peer.getId());
        agentCtx.getChannelMap().remove(peer.getChannel());
    }
}
