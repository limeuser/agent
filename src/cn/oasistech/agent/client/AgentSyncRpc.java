package cn.oasistech.agent.client;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.oasistech.agent.*;
import cn.oasistech.util.Logger;
import cn.oasistech.util.SyncClient;
import cn.oasistech.util.Tag;

public class AgentSyncRpc {
    private SyncClient client;
    private AgentParser parser;
    private byte[] recvBuffer = new byte[2048];
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public AgentSyncRpc(SyncClient client, AgentParser parser) {
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
    
    public SetTagResponse setTag(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        return setTag(tags);
    }
    
    public SetTagResponse setTag(List<Tag> tags) {
        SetTagRequest request = new SetTagRequest();
        request.setTags(tags);
        return (SetTagResponse) process(request);
    }
    
    public GetTagResponse getTag(int id, String ...keys) {
        IdKey idKey = new IdKey();
        idKey.setId(id);
        for (String key : keys) {
            idKey.getKeys().add(key);
        }
        List<IdKey> idKeys = new ArrayList<IdKey>();
        idKeys.add(idKey);
        return getTag(idKeys);
    }
    
    public GetTagResponse getTag(List<IdKey> idKeys) {
        GetTagRequest request = new GetTagRequest();
        request.setIdKeys(idKeys);
        return (GetTagResponse) process(request);
    }
    
    public GetIdResponse getId(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        return getId(tags);
    }
    
    public GetIdResponse getId(List<Tag> tags) {
        GetIdRequest request = new GetIdRequest();
        request.setTags(tags);
        return (GetIdResponse) process(request);
    }
    
    public GetMyIdResponse getMyId() {
        GetMyIdRequest request = new GetMyIdRequest();
        return (GetMyIdResponse) process(request);
    }
    
    public SetIdResponse setId(int id) {
        SetIdRequest request = new SetIdRequest();
        request.setId(id);
        return (SetIdResponse) process(request);
    }
    
    private Response process(Request request) {
        logger.log("sync rpc send request:%s", request.toString());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] requestBuf = parser.encodeRequest(request);
        AgentProtocol.write(out, requestBuf);
        if (client.send(out.toByteArray()) == false) {
            logger.log("can't send data");
            return null;
        }
        int length = client.recv(recvBuffer);
        if (length == 0) {
            logger.log("can't receive data");
            return null;
        }
        
        ByteArrayOutputStream in = new ByteArrayOutputStream();
        in.write(recvBuffer, AgentProtocol.IdHeadLength, length - AgentProtocol.IdHeadLength);
        Response response = parser.decodeResponse(in.toByteArray());

        logger.log("sync rpc recv response:%s", response.toString());
        
        return response;
    }
}
