package cn.oasistech.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.List;

import mjoys.util.Logger;
import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentMsgSerializer;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.NotifyConnectionResponse;
import cn.oasistech.agent.Peer;
import cn.oasistech.agent.Request;
import cn.oasistech.agent.Response;

public class IdFrameHandler extends ChannelInboundHandlerAdapter {
    private AgentMsgSerializer parser;
    private AgentHandler<Channel> handler;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public IdFrameHandler(AgentContext<Channel> agentCtx, AgentHandler<Channel> handler, AgentMsgSerializer parser) {
        this.handler = handler;
        this.agentCtx = agentCtx;
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
    
            if (peer == null) {
                peer = addNewPeer(ctx.channel());
                logger.log("add new peer when channelRead:%s", peer.toString());
            }

            IdFrame idFrame = (IdFrame) msg;
            if (idFrame.getId() == AgentProtocol.PublicService.Agent.id) {
                processRequest(peer, idFrame);
            } else {
                route(peer, idFrame);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
    
    private void processRequest(Peer<Channel> peer, IdFrame idFrame) {
        Request request = parser.decodeRequest(idFrame.getBody());
        if (request == null) {
            logger.log("request is null");
            sendError(peer.getChannel(), AgentProtocol.MsgType.Unknown, AgentProtocol.Error.BadMessageFormat);
            return;
        }
        
        List<Peer<Channel>> listenersBefore = null;
        if (request.getType().equals(AgentProtocol.MsgType.SetTag)) {
            listenersBefore = agentCtx.getListeners(peer);
        }
        
        Response response = handler.processRequest(peer, request);
        if (response == null) {
            sendError(peer.getChannel(), AgentProtocol.MsgType.Unknown, AgentProtocol.Error.InvalidRequest);
            return;
        }
        
        byte[] data = parser.encodeResponse(response);
        sendData(peer.getChannel(), AgentProtocol.PublicService.Agent.id, data, data.length);
        
        // notify connection changed
        List<Peer<Channel>> listenersAfter = null;
        if (request.getType().equals(AgentProtocol.MsgType.SetTag.name())) {
            listenersAfter = agentCtx.getListeners(peer);
            
            for (Peer<Channel> oldListener : listenersBefore) {
                boolean has = false;
                for (Peer<Channel> newListener : listenersAfter) {
                    if (newListener == oldListener) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    notifyConnection(peer, NotifyConnectionResponse.Action.disconnect);
                }
            }
            
            for (Peer<Channel> newListener : listenersAfter) {
                boolean has = false;
                for (Peer<Channel> oldListener : listenersBefore) {
                    if (newListener == oldListener) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    notifyConnection(peer, NotifyConnectionResponse.Action.connect);
                }
            }
        }
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

        sendData(dstHost.getChannel(), srcId, idFrame.getBody(), idFrame.getBodyLength());
        logger.log("route a message from %s to %s", srcHost.toString(), dstHost.toString());
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
        
        notifyConnection(peer, NotifyConnectionResponse.Action.connect);
        
        return peer;
    }
    
    private void removeDisconnectedPeer(Peer<Channel> peer) {
        agentCtx.getIdMap().remove(peer.getId());
        agentCtx.getChannelMap().remove(peer.getChannel());
        
        notifyConnection(peer, NotifyConnectionResponse.Action.disconnect);
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
    
    private void notifyConnection(Peer<Channel> connectionPeer, NotifyConnectionResponse.Action action) {
        for (Peer<Channel> peer : agentCtx.getIdMap().values()) {
            Response response = this.handler.getNotifyConnectionResponse(peer, connectionPeer, action);
            if (response != null) {
                byte[] data = parser.encodeResponse(response);
                sendData(peer.getChannel(), AgentProtocol.PublicService.Agent.id, data, data.length);
            }
        }
    }
    
    private void sendError(Channel channel, AgentProtocol.MsgType type, AgentProtocol.Error error) {
        Response response = new Response();
        response.setType(type);
        response.setError(error);
        byte[] data = parser.encodeResponse(response);
        sendData(channel, AgentProtocol.PublicService.Agent.id, data, data.length);
    }
    
    public final static void sendData(Channel channel, int id, byte[] data, int dataLength) {
        ByteBuf writeBuffer = Unpooled.buffer();
        writeBuffer.writeInt(id);
        writeBuffer.writeInt(data.length);
        writeBuffer.writeBytes(data, 0, dataLength);
        channel.writeAndFlush(writeBuffer);
    }
}
