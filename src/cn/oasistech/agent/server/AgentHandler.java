package cn.oasistech.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mjoys.io.Serializer;
import mjoys.util.Logger;
import mjoys.util.StringUtil;
import cn.oasistech.agent.AgentContext;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdRequest;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.GetIdTagRequest;
import cn.oasistech.agent.GetIdTagResponse;
import cn.oasistech.agent.GetMyIdRequest;
import cn.oasistech.agent.GetMyIdResponse;
import cn.oasistech.agent.GetTagRequest;
import cn.oasistech.agent.GetTagResponse;
import cn.oasistech.agent.IdKey;
import cn.oasistech.agent.IdTag;
import cn.oasistech.agent.ListenConnectionRequest;
import cn.oasistech.agent.ListenConnectionResponse;
import cn.oasistech.agent.NotifyConnectionResponse;
import cn.oasistech.agent.Peer;
import cn.oasistech.agent.Response;
import cn.oasistech.agent.SetIdRequest;
import cn.oasistech.agent.SetIdResponse;
import cn.oasistech.agent.SetTagRequest;
import cn.oasistech.agent.SetTagResponse;
import cn.oasistech.util.Tag;

public class AgentHandler<Channel> {
	private Serializer serializer;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public AgentHandler(AgentContext<Channel> agentCtx, Serializer serializer) {
        this.agentCtx = agentCtx;
        this.serializer = serializer;
    }
    
    public Response processRequest(Peer<Channel> peer, AgentProtocol.MsgType requestType, ByteBuf buf) {
        Response response = null;
        try {
	        if (requestType == AgentProtocol.MsgType.SetId) {
	            response = setId(peer, serializer.decode(new ByteBufInputStream(buf), SetIdRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.GetId) {
	            response = getId(serializer.decode(new ByteBufInputStream(buf), GetIdRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.GetMyId) {
	            response = getMyId(peer, serializer.decode(new ByteBufInputStream(buf), GetMyIdRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.SetTag) {
	            response = setTag(peer, serializer.decode(new ByteBufInputStream(buf), SetTagRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.GetTag) {
	            response = getTag(serializer.decode(new ByteBufInputStream(buf), GetTagRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.GetIdTag) {
	        	response = getIdTag(serializer.decode(new ByteBufInputStream(buf), GetIdTagRequest.class));
	        }else if (requestType == AgentProtocol.MsgType.ListenConnection) {
	            response = listenConnection(peer, serializer.decode(new ByteBufInputStream(buf), ListenConnectionRequest.class));
	        }else {
	            return null;
	        }
        } catch (Exception e) {
        	logger.log("parse request exception", e);
        	return null;
        }
        
        logger.log("send response:%s", response.toString());
        
        return response;
    }
    
    public Response setId(Peer<Channel> peer, SetIdRequest request) {
        SetIdResponse response = new SetIdResponse();
        int newId = request.getId();
        Peer<Channel> newPeer = agentCtx.getIdMap().get(newId);
        if (newPeer != null) {
            response.setError(AgentProtocol.Error.IdExisted);
            return response;
        }

        agentCtx.getIdMap().remove(peer.getId());
        agentCtx.getIdMap().put(newId, peer);
        peer.setId(newId);
        return response;
    }
    
    public Response getId(GetIdRequest request) {
        GetIdResponse response = new GetIdResponse();
        for (Peer<Channel> p : agentCtx.getPeerByTag(request.getTags())) {
            response.getIds().add(p.getId());
        }
        return response;
    }
    
    public Response getMyId(Peer<Channel> peer, GetMyIdRequest request) {
        GetMyIdResponse response = new GetMyIdResponse();
        response.setId(peer.getId());
        return response;
    }

    public Response setTag(Peer<Channel> peer, SetTagRequest request) {
        SetTagResponse response = new SetTagResponse();
        for (Tag tag : request.getTags()) {
            peer.getTags().put(tag.getKey(), tag.getValue());
        }
        
        return response;
    }
    
    public Response getTag(GetTagRequest request) {
        GetTagResponse response = new GetTagResponse();
        
        // 如果id是无效id，获取所有连接的tag
        if (request.getIdKeys().size() == 1 && request.getIdKeys().get(0).getId() == AgentProtocol.InvalidId) {
            IdKey idKey = request.getIdKeys().get(0);
            for (Peer<Channel> peer : agentCtx.getChannelMap().values()) {
                addIdTag(response.getIdTags(), peer, idKey.getKeys());
            }
        } else {
            for (IdKey idKey : request.getIdKeys()) {
                Peer<Channel> peer = agentCtx.getIdMap().get(idKey.getId());
                if (peer != null) {
                	addIdTag(response.getIdTags(), peer, idKey.getKeys());
                }
            }
        }
        return response;
    }
    
    private void addIdTag(List<IdTag> idTags, Peer<Channel> peer, List<String> keys) {
    	IdTag idTag = new IdTag();
        idTag.setId(peer.getId());
        idTag.setTags(getTags(peer, keys));
        if (idTag.getTags().isEmpty() == false) {
        	idTags.add(idTag);
        }
    }
    
    private List<Tag> getTags(Peer<Channel> peer, List<String> keys) {
        List<Tag> tags = new ArrayList<Tag>();
        
        // 如果没有指定key，获取所有tag
        if (keys.isEmpty()) {
            for (Entry<String, String> tag : peer.getTags().entrySet()) {
                tags.add(new Tag(tag.getKey(), tag.getValue()));
            }
        } else {
            for (String key : keys) {
                if (peer.getTags().containsKey(key)) {
                    Tag tag = new Tag(key, peer.getTags().get(key));
                    tags.add(tag);
                }
            }
        }
        
        return tags;
    }
    
    private GetIdTagResponse getIdTag(GetIdTagRequest request) {
    	GetIdTagResponse response = new GetIdTagResponse();
    	
    	// firstly, get id by tags which is not empty
    	List<Tag> tags = new ArrayList<Tag>();
    	List<String> keys = new ArrayList<String>();
    	for (Tag tag : request.getTags()) {
    		if (StringUtil.isNotEmpty(tag.getValue())) {
    			tags.add(tag);
    		}
    		
    		keys.add(tag.getKey());
    	}
    	
    	// secondly, fill all tags 
    	for (Peer<Channel> peer : agentCtx.getPeerByTag(tags)) {
    		addIdTag(response.getIdTags(), peer, keys);
    	}
    	
    	return response;
    }
    
    private ListenConnectionResponse listenConnection(Peer<Channel> peer, ListenConnectionRequest request) {
        peer.getListeners().add(request.getIdTag());
        return new ListenConnectionResponse();
    }
    
    public Response getNotifyConnectionResponse(Peer<Channel> listeningPeer, Peer<Channel> connection, NotifyConnectionResponse.Action action) {
        if (listeningPeer.isListening(connection.getTags())) {
            NotifyConnectionResponse response = new NotifyConnectionResponse();
            IdTag idTag = new IdTag();
            idTag.setId(connection.getId());
            idTag.setTags(connection.getTagList());
            response.setIdTag(idTag);
            response.setAction(action);
            return response;
        }
        
        return null;
    }
}
