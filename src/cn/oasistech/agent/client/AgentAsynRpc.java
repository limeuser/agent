package cn.oasistech.agent.client;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.oasistech.agent.*;
import cn.oasistech.util.AsynClient;
import cn.oasistech.util.Logger;
import cn.oasistech.util.Tag;

public class AgentAsynRpc {
    private AsynClient client;
    private AgentParser parser;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public AgentAsynRpc(AsynClient client, AgentParser parser) {
        this.client = client;
        this.parser = parser;
    }
    
    public void sendTo(int id, byte[] body, int offset, int length) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        AgentProtocol.write(buffer, id, body, offset, length);
        client.send(buffer.toByteArray());
    }
    
    public void sendTo(int id, byte[] body) {
        sendTo(id, body, 0, body.length);
    }
    
    public void sendToAgent(byte[] body) {
        sendTo(AgentProtocol.PublicService.Agent.id, body);
    }
    
    public void sendRequest(Request request) {
        logger.log("asyn rpc send request:%s", request.toString());
        sendToAgent(parser.encodeRequest(request));
    }
    
    public void setTag(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        setTag(tags);
    }
    
    public void setTag(List<Tag> tags) {
        SetTagRequest request = new SetTagRequest();
        request.setTags(tags);
        sendRequest(request);
    }
    
    public void getTag(int id, String ...keys) {
        IdKey idKey = new IdKey();
        idKey.setId(id);
        for (String key : keys) {
            idKey.getKeys().add(key);
        }
        List<IdKey> idKeys = new ArrayList<IdKey>();
        idKeys.add(idKey);
        getTag(idKeys);
    }
    
    public void getTag(List<IdKey> idKeys) {
        GetTagRequest request = new GetTagRequest();
        request.setIdKeys(idKeys);
        sendRequest(request);
    }
    
    public void getId(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        getId(tags);
    }
    
    public void getId(List<Tag> tags) {
        GetIdRequest request = new GetIdRequest();
        request.setTags(tags);
        sendRequest(request);
    }
    
    public void getMyId() {
        sendRequest(new GetMyIdRequest());
    }
    
    public void setId(int id) {
        SetIdRequest request = new SetIdRequest();
        request.setId(id);
        sendRequest(request);
    }
}
