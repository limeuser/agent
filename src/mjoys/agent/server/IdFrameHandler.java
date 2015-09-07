package mjoys.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.List;

import mjoys.agent.Agent;
import mjoys.agent.AgentContext;
import mjoys.agent.NotifyConnectionResponse;
import mjoys.agent.Peer;
import mjoys.agent.Response;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.Serializer;
import mjoys.io.SerializerException;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class IdFrameHandler extends ChannelInboundHandlerAdapter {
    private Serializer serializer;
    private AgentHandler<Channel> handler;
    private AgentContext<Channel> agentCtx;
    private ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public IdFrameHandler(AgentContext<Channel> agentCtx, AgentHandler<Channel> handler, Serializer serializer) {
        this.handler = handler;
        this.agentCtx = agentCtx;
        this.serializer = serializer;
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
        Peer<Channel> peer = agentCtx.getChannelMap().get(ctx.channel());

        if (peer == null) {
            peer = addNewPeer(ctx.channel());
            logger.log("add new peer when channelRead:%s", peer.toString());
        }
        
        @SuppressWarnings("unchecked")
		TLV<ByteBuf> idFrame = (TLV<ByteBuf>)msg;
        if (idFrame.tag == Agent.PublicService.Agent.id) {
            processRequest(peer, idFrame);
            idFrame.body.release();
        } else {
            route(peer, idFrame);
        }
    }

    private void processRequest(Peer<Channel> peer, TLV<ByteBuf> idFrame) {
    	TV<ByteBuf> requestFrame = FrameParser.parseAgentMsgFrame(idFrame.body);
    	if (requestFrame == null) {
    		logger.log("bad agent request frame");
    		return;
    	}

    	Agent.MsgType requestType = Agent.getMsgType(requestFrame.tag);
    	if (requestType == Agent.MsgType.Unknown) {
            logger.log("request is unknown");
            sendError(peer.getChannel(), Agent.MsgType.Unknown, Agent.Error.InvalidRequest);
            return;
        }
        
        List<Peer<Channel>> listenersBefore = null;
        if (requestType == Agent.MsgType.SetTag) {
            listenersBefore = agentCtx.getListeners(peer);
        }
        
        Response response = handler.processRequest(peer, requestType, idFrame.body);
        if (response == null) {
            sendError(peer.getChannel(), Agent.MsgType.Unknown, Agent.Error.InternalError);
            return;
        }
        
        sendData(peer.getChannel(), Agent.PublicService.Agent.id, requestType, response);
        
        // notify connection changed
        List<Peer<Channel>> listenersAfter = null;
        if (requestType == Agent.MsgType.SetTag) {
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
            idFrame.body.release();
            sendError(srcHost.getChannel(), Agent.MsgType.Route, Agent.Error.NoConnection);
            return;
        }

        idFrame.body.setInt(0, srcHost.getId());
        idFrame.body.resetReaderIndex();
        dstHost.getChannel().writeAndFlush(idFrame.body);
        logger.log("route a message:%d from %s to %s", idFrame.body.readableBytes(), srcHost.toString(), dstHost.toString());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isWritable()) {
            sendError(ctx.channel(), Agent.MsgType.Unknown, Agent.Error.InternalError);
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
        peer.getTags().put(Agent.PublicTag.id.name(), String.valueOf(peer.getId()));
        peer.getTags().put(Agent.PublicTag.address.name(), address.getAddress().getHostAddress());
        peer.getTags().put(Agent.PublicTag.name.name(), address.getAddress().getHostName());
        peer.getTags().put(Agent.PublicTag.port.name(), String.valueOf(address.getPort()));
        
        logger.log("new peer: id=%d, name=%s, address=%s, port=%d", 
                peer.getId(), 
                address.getAddress().getHostName(), 
                address.getAddress().getHostAddress(), 
                address.getPort());
    }
    
    private void notifyConnection(Peer<Channel> connectionPeer, NotifyConnectionResponse.Action action) {
        for (Peer<Channel> peer : agentCtx.getIdMap().values()) {
        	if (peer.isListening(connectionPeer.getTags())) {
	            Response response = this.handler.getNotifyConnectionResponse(peer, connectionPeer, action);
	            if (response != null) {
	                sendData(peer.getChannel(), Agent.PublicService.Agent.id, Agent.MsgType.NotifyConnection, response);
	            }
        	}
        }
    }
    
    private void sendError(Channel channel, Agent.MsgType type, Agent.Error error) {
        Response response = new Response();
        response.setError(error);
        sendData(channel, Agent.PublicService.Agent.id, type, response);
    }
    
    public void sendData(Channel channel, int id, Agent.MsgType type, Object body) {
    	logger.log("agent send msg=%s:%s", type.name(), StringUtil.toString(body));
    	
    	ByteBuf sendBuf = alloc.directBuffer();
    	sendBuf.clear();
    	sendBuf.writerIndex(Agent.HeadLength + Agent.TypeFieldLength);
    	try {
    		serializer.encode(body, new ByteBufOutputStream(sendBuf));
    	} catch(SerializerException e) {
    		logger.log("serializer response exception", e);
    		return;
    	}
    	sendBuf.setInt(0, id);
    	sendBuf.setInt(4, sendBuf.readableBytes() - Agent.HeadLength);
    	sendBuf.setInt(Agent.HeadLength, type.ordinal());
    	channel.writeAndFlush(sendBuf);
    }
}
