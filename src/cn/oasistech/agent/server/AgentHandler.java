package cn.oasistech.agent.server;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import cn.oasistech.agent.*;
import cn.oasistech.util.Logger;
import cn.oasistech.util.StringUtil;
import cn.oasistech.util.Tag;

public class AgentHandler<Channel> {
    private AgentParser parser;
    private AgentContext<Channel> agentCtx;
    private Logger logger = new Logger().addPrinter(System.out);
    
    public AgentHandler(AgentContext<Channel> agentCtx, AgentParser parser) {
        this.agentCtx = agentCtx;
        this.parser = parser;
    }
    
    public byte[] processRequest(Peer<Channel> peer, byte[] buffer) {
        Request request = parser.decodeRequest(buffer);
        if (request == null) {
            logger.log("request is null");
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            AgentProtocol.writeHead(outBuffer, peer.getId(), 0);
            return outBuffer.toByteArray();
        }
        
        Response response = null;
        if (request instanceof SetIdRequest) {
            response = setId(peer, (SetIdRequest)request);
        }else if (request instanceof GetIdRequest) {
            response = getId((GetIdRequest)request);
        }else if (request instanceof GetMyIdRequest) {
            response = getMyId(peer, (GetMyIdRequest)request);
        }else if (request instanceof SetTagRequest) {
            response = setTag(peer, (SetTagRequest)request);
        }else if (request instanceof GetTagRequest) {
            response = getTag((GetTagRequest)request);
        }else if (request instanceof GetIdTagRequest) {
        	response = getIdTag((GetIdTagRequest)request);
        }
        
        logger.log("recv request:%s", request.toString());
        logger.log("send response:%s", response.toString());
        
        byte[] out = parser.encodeResponse(response);
        if (out == null) {
            logger.log("response is null");
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            AgentProtocol.writeHead(outBuffer, peer.getId(), 0);
            return outBuffer.toByteArray();
        }
        
        return out;
    }
    
    public Response setId(Peer<Channel> peer, SetIdRequest request) {
        SetIdResponse response = new SetIdResponse();
        int newId = request.getId();
        Peer<Channel> newPeer = agentCtx.getIdMap().get(newId);
        if (newPeer != null) {
            response.setError(AgentProtocol.Error.IdExisted.name());
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
    
    public AgentParser getAgentParser() {
        return parser;
    }
}
