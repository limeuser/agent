package cn.oasistech.agent.client;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.oasistech.agent.*;
import cn.oasistech.util.Address;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.ClassUtil;
import cn.oasistech.util.Logger;
import cn.oasistech.util.SocketClient;
import cn.oasistech.util.Tag;

public class AgentSyncRpc {
    private SocketClient client;
    private AgentParser parser;
    private byte[] recvBuffer = new byte[2048];
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        this.parser = ClassUtil.newInstance(Cfg.getParserClassName());
        if (this.parser == null) {
            return false;
        }
        
        this.client = new SocketClient();
        return client.start(address);
    }
    
    public void stop() {
        if (this.client != null) {
            this.client.stop();
        }
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
    
    public boolean sendTo(int id, byte[] body, int offset, int length) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AgentProtocol.write(out, id, body, offset, length);
        return client.send(out.toByteArray());
    }
    
    public boolean sendTo(int id, byte[] body) {
        return sendTo(id, body, 0, body.length);
    }
    
    public IdFrame recv() {
        int length = client.recv(recvBuffer);
        if (length < AgentProtocol.IdHeadLength) {
            return null;
        }
        
        return AgentProtocol.parseIdFrame(recvBuffer);
    }
    
    public IdFrame call(int id, byte[] body, int offset, int bodyLength) {
        if (sendTo(id, body, offset, bodyLength) == true) {
            return recv();
        }
        return null;
    }
    
    public IdFrame call(int id, byte[] body) {
        return call(id, body, 0, body.length);
    }
    
    private Response process(Request request) {
        logger.log("sync rpc send request:%s", request.toString());
        
        byte[] requestBuf = parser.encodeRequest(request);
        IdFrame frame = call(AgentProtocol.PublicService.Agent.id, requestBuf);
        if (frame == null) {
            return null;
        }
        
        Response response = parser.decodeResponse(frame.getBody());
        logger.log("sync rpc recv response:%s", response.toString());
        return response;
    }
}
