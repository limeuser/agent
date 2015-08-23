package cn.oasistech.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.List;

import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.Serializer;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.NotifyConnectionResponse;
import cn.oasistech.agent.Peer;
import cn.oasistech.agent.Response;

public class IdFrameHandler extends ChannelInboundHandlerAdapter {
    private Serializer parser;
    private AgentHandler<Channel> handler;
    private AgentContext<Channel> agentCtx;
    private ByteBuf sendBuf;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public IdFrameHandler(AgentContext<Channel> agentCtx, AgentHandler<Channel> handler, Serializer parser) {
        this.handler = handler;
        this.agentCtx = agentCtx;
        this.sendBuf = Unpooled.directBuffer(ByteUnit.KB);
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
            
            @SuppressWarnings("unchecked")
			TLV<ByteBuf> idFrame = (TLV<ByteBuf>)msg;
            if (idFrame.tag == AgentProtocol.PublicService.Agent.id) {
                processRequest(peer, idFrame);
            } else {
                route(peer, idFrame);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void processRequest(Peer<Channel> peer, TLV<ByteBuf> idFrame) {
    	TV<ByteBuf> requestFrame = FrameParser.parseAgentMsgFrame(idFrame.body);
    	if (requestFrame == null) {
    		logger.log("bad agent request frame");
    		return;
    	}

    	AgentProtocol.MsgType requestType = AgentProtocol.getMsgType(requestFrame.tag);
    	if (requestType == AgentProtocol.MsgType.Unknown) {
            logger.log("request is unknown");
            sendError(peer.getChannel(), AgentProtocol.MsgType.Unknown, AgentProtocol.Error.BadMessageFormat);
            return;
        }
        
        List<Peer<Channel>> listenersBefore = null;
        if (requestType == AgentProtocol.MsgType.SetTag) {
            listenersBefore = agentCtx.getListeners(peer);
        }
        
        Response response = handler.processRequest(peer, requestType, idFrame.body);
        if (response == null) {
            sendError(peer.getChannel(), AgentProtocol.MsgType.Unknown, AgentProtocol.Error.InvalidRequest);
            return;
        }
        
        sendData(peer.getChannel(), AgentProtocol.PublicService.Agent.id, requestType, response);
        
        // notify connection changed
        List<Peer<Channel>> listenersAfter = null;
        if (requestType == AgentProtocol.MsgType.SetTag) {
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

    private void route(Peer<Channel> srcHost, TLV<ByteBuf> idFrame) {
        // route request: replace dst-id with src-id in frame
        int dstId = idFrame.tag;

        Peer<Channel> dstHost = agentCtx.getIdMap().get(dstId);
        if (dstHost == null) {
            logger.log("can't find router connection destid=%d", dstId);
            sendError(srcHost.getChannel(), AgentProtocol.MsgType.Route, AgentProtocol.Error.NoConnection);
            return;
        }

        idFrame.body.setInt(0, dstId);
        idFrame.body.resetReaderIndex();
        dstHost.getChannel().write(idFrame.body);
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
                sendData(peer.getChannel(), AgentProtocol.PublicService.Agent.id, AgentProtocol.MsgType.NotifyConnection, response);
            }
        }
    }
    
    private void sendError(Channel channel, AgentProtocol.MsgType type, AgentProtocol.Error error) {
        Response response = new Response();
        response.setError(error);
        sendData(channel, AgentProtocol.PublicService.Agent.id, type, response);
    }
    
    public void sendData(Channel channel, int id, AgentProtocol.MsgType type, Object body) {
    	this.sendBuf.clear();
    	this.sendBuf.writerIndex(AgentProtocol.HeadLength + AgentProtocol.TypeFieldLength);
    	try {
    		parser.encode(body, new ByteBufOutputStream(this.sendBuf));
    	} catch(Exception e) {
    		logger.log("serializer response exception", e);
    		return;
    	}
    	int length = this.sendBuf.writerIndex() - AgentProtocol.HeadLength - AgentProtocol.TypeFieldLength;
    	this.sendBuf.resetWriterIndex();
    	this.sendBuf.writeInt(id);
    	this.sendBuf.writeInt(length);
    	this.sendBuf.writeInt(type.ordinal());
    	channel.write(this.sendBuf);
    }
}
