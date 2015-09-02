package mjoys.agent.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mjoys.agent.*;
import mjoys.agent.util.Tag;
import mjoys.io.Serializer;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class AgentHandler<Channel> {
	private Serializer serializer;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public AgentHandler(AgentContext<Channel> agentCtx, Serializer serializer) {
        this.agentCtx = agentCtx;
        this.serializer = serializer;
    }
    
    public Response processRequest(Peer<Channel> peer, Agent.MsgType requestType, ByteBuf buf) {
        Response response = null;
        try {
	        if (requestType == Agent.MsgType.SetId) {
	            response = setId(peer, serializer.decode(new ByteBufInputStream(buf), SetIdRequest.class));
	        }else if (requestType == Agent.MsgType.GetId) {
	            response = getId(serializer.decode(new ByteBufInputStream(buf), GetIdRequest.class));
	        }else if (requestType == Agent.MsgType.GetMyId) {
	            response = getMyId(peer);
	        }else if (requestType == Agent.MsgType.SetTag) {
	            response = setTag(peer, serializer.decode(new ByteBufInputStream(buf), SetTagRequest.class));
	        }else if (requestType == Agent.MsgType.GetTag) {
	            response = getTag(serializer.decode(new ByteBufInputStream(buf), GetTagRequest.class));
	        }else if (requestType == Agent.MsgType.GetIdTag) {
	        	response = getIdTag(serializer.decode(new ByteBufInputStream(buf), GetIdTagRequest.class));
	        }else if (requestType == Agent.MsgType.ListenConnection) {
	            response = listenConnection(peer, serializer.decode(new ByteBufInputStream(buf), ListenConnectionRequest.class));
	        }else {
	            return null;
	        }
        } catch (Exception e) {
        	logger.log("parse request exception", e);
        	return null;
        }

        return response;
    }
    
    public Response setId(Peer<Channel> peer, SetIdRequest request) {
        SetIdResponse response = new SetIdResponse();
        int newId = request.getId();
        Peer<Channel> newPeer = agentCtx.getIdMap().get(newId);
        if (newPeer != null) {
            response.setError(Agent.Error.IdExisted);
            return response;
        }

        agentCtx.getIdMap().remove(peer.getId());
        agentCtx.getIdMap().put(newId, peer);
        peer.setId(newId);
        logger.log("SetId: request:%s, response:%s", request, response);
        return response;
    }
    
    public Response getId(GetIdRequest request) {
        GetIdResponse response = new GetIdResponse();
        for (Peer<Channel> p : agentCtx.getPeerByTag(request.getTags())) {
            response.getIds().add(p.getId());
        }
        logger.log("GetId: request:%s, response:%s", request, response);
        return response;
    }
    
    public Response getMyId(Peer<Channel> peer) {
        GetMyIdResponse response = new GetMyIdResponse();
        response.setId(peer.getId());
        logger.log("GetMyId: response:%s", response);
        return response;
    }

    public Response setTag(Peer<Channel> peer, SetTagRequest request) {
        SetTagResponse response = new SetTagResponse();
        for (Tag tag : request.getTags()) {
            peer.getTags().put(tag.getKey(), tag.getValue());
        }
        logger.log("SetTag: request:%s, response:%s", request, response);
        return response;
    }
    
    public Response getTag(GetTagRequest request) {
        GetTagResponse response = new GetTagResponse();
        
        // 如果id是无效id，获取所有连接的tag
        if (request.getIdKeys().size() == 1 && request.getIdKeys().get(0).getId() == Agent.InvalidId) {
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
        
        logger.log("GetTag: request:%s, response:%s", request, response);
        
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
    	
    	logger.log("GetIdTag: request:%s, response:%s", request, response);
    	
    	return response;
    }
    
    private ListenConnectionResponse listenConnection(Peer<Channel> peer, ListenConnectionRequest request) {
        Map<String, String> tags = new HashMap<String, String>();
        for (Tag tag : request.getTags()) {
            tags.put(tag.getKey(), tag.getValue());
        }
        peer.setListenTags(tags);
        
        logger.log("ListenConnection: request:%s", request);
        
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
            logger.log("NotifyConnection: response:%s", response);
            return response;
        }
        
        return null;
    }
}
