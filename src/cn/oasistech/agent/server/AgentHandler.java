package cn.oasistech.agent.server;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map.Entry;

import cn.oasistech.agent.*;
import cn.oasistech.util.Logger;
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
            for (Peer<Channel> thePeer : agentCtx.getChannelMap().values()) {
                getTag(thePeer, idKey, response.getIdTags());
            }
        } else {
            for (IdKey idKey : request.getIdKeys()) {
                Peer<Channel> peer = agentCtx.getIdMap().get(idKey.getId());
                if (peer != null) {
                    getTag(peer, idKey, response.getIdTags());
                }
            }
        }
        return response;
    }
    
    private void getTag(Peer<Channel> peer, IdKey idKey, List<IdTag> idTags) {
        IdTag idTag = new IdTag();
        idTag.setId(peer.getId());
        // 如果没有指定key，获取所有tag
        if (idKey.getKeys().isEmpty()) {
            for (Entry<String, String> tag : peer.getTags().entrySet()) {
                idTag.getTags().add(new Tag(tag.getKey(), tag.getValue()));
            }
        } else {
            for (String key : idKey.getKeys()) {
                if (peer.getTags().containsKey(key)) {
                    Tag tag = new Tag(key, peer.getTags().get(key));
                    idTag.getTags().add(tag);
                }
            }
        }
        
        if (idTag.getTags().isEmpty() == false) {
            idTags.add(idTag);
        }
    }
    
    public AgentParser getAgentParser() {
        return parser;
    }
}
