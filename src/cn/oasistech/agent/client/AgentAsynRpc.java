package cn.oasistech.agent.client;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import mjoys.io.Serializer;
import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ClassUtil;
import mjoys.util.Logger;
import cn.oasistech.agent.*;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.Tag;

public class AgentAsynRpc {
    private SocketClient client;
    private Serializer serializer;
    private AgentRpcHandler handler;
    private byte[] buffer;
    private Thread readerThread;
    private boolean readingData;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address serverAddress, AgentRpcHandler handler) {
        this.serializer = ClassUtil.newInstance(Cfg.getParserClassName());
        if (serializer == null) {
            return false;
        }
        
        this.client = new SocketClient();
        boolean success = this.client.connect(serverAddress);
        if (success == false) {
            this.client = null;
            return false;
        }
        
        this.handler = handler;
        this.buffer = new byte[2048];
        
        this.readerThread = new Thread(new Reader(this));
        this.readingData = true;
        this.readerThread.start();
        
        return true;
    }
    
    public void stop() {
        this.readingData = false;
        
        if (this.client != null) {
            this.client.disconnect();
        }
    }
    
    public class Reader implements Runnable {
        private AgentAsynRpc rpc;
        
        public Reader(AgentAsynRpc rpc) {
            this.rpc = rpc;
        }
        
        @Override
        public void run() {
            while (readingData) {
                int length = client.recv(buffer);
                IdFrame frame = AgentProtocol.parseIdFrame(buffer);
                if (length > 0) {
                    handler.handle(rpc, frame);
                }
            }
        }
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
        sendToAgent(serializer.encodeRequest(request));
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
    
    public void getIdTag(List<Tag> tags) {
    	GetIdTagRequest request = new GetIdTagRequest();
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
    
    public void listenConnection(List<Tag> tags) {
        ListenConnectionRequest request = new ListenConnectionRequest();
        request.setTags(tags);
        sendRequest(request);
    }
    
    public AgentMsgSerializer getSerializer() {
        return serializer;
    }
}
