package mjoys.agent.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mjoys.agent.Agent;
import mjoys.agent.GetIdRequest;
import mjoys.agent.GetIdTagRequest;
import mjoys.agent.GetTagRequest;
import mjoys.agent.IdKey;
import mjoys.agent.ListenConnectionRequest;
import mjoys.agent.SetIdRequest;
import mjoys.agent.SetTagRequest;
import mjoys.agent.util.AgentCfg;
import mjoys.agent.util.Tag;
import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.io.Serializer;
import mjoys.io.SerializerException;
import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.ClassUtil;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class AgentAsynRpc {
    private SocketClient client;
    private Serializer serializer;
    private AgentRpcHandler<ByteBuffer> handler;
    private byte[] recvBuffer;
    private byte[] sendBuffer;
    private Thread readerThread;
    private boolean readingData;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address serverAddress, AgentRpcHandler<ByteBuffer> handler) {
        this.serializer = ClassUtil.newInstance(AgentCfg.instance.getSerializerClassName());
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
        this.recvBuffer = new byte[ByteUnit.KB];
        this.sendBuffer = new byte[ByteUnit.KB];
        
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
            	int length = 0;
            	try {
            		length = client.recv(recvBuffer);
            		if (length <= 0) {
            			continue;
            		}
            		TLV<ByteBuffer> idFrame = ByteBufferParser.parseTLV(ByteBuffer.wrap(recvBuffer, 0, length));
            		if (idFrame == null) {
            			
            		} else {
                        handler.handle(rpc, idFrame);
                    }
            	} catch (SocketException e) {
            		if (readingData == true)
            			client.reconnect();
            	} catch(IOException e) {
            		logger.log("agent asyn rpc recv exception", e);
            	}
            }
        }
    }

    public void send(int id, ByteBuffer body) {
    	ByteBuffer byteBuffer = ByteBuffer.wrap(sendBuffer);
    	
    	try {
    		Agent.encodeBody(id, byteBuffer, body);
			client.send(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
    	} catch (SocketException e) {
    		logger.log("agent asyn rpc socket exception, reconnect", e);
    		client.reconnect();
    	} catch (IOException e) {
    		logger.log("agent asyn rpc send body io excetion:%s", e);
    	}
    }
    
    public void sendMsg(int id, int msgType, Object msg) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(sendBuffer);
        
        try {
	        Agent.encodeMsg(id, msgType, msg, byteBuffer, serializer);
	    	client.send(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
    	} catch (SocketException e) {
    		logger.log("agent asyn rpc socket exception, reconnect", e);
    		client.reconnect();
    	} catch (IOException e) {
    		logger.log("agent asyn rpc send msg io excetion:%s", e, StringUtil.toString(msg));
    	} catch (SerializerException e) {
    		logger.log("serializer exception: msg=%s", e, StringUtil.toString(msg));
    	}
    }
    
    private void sendRequest(Agent.MsgType msgType, Object msg) {
    	logger.log("asyn send agent msg: %s:%s", msgType.name(), StringUtil.toString(msg));
    	sendMsg(Agent.PublicService.Agent.id, msgType.ordinal(), msg);
    }
    
    public void setTag(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        setTag(tags);
    }
    
    public void setTag(List<Tag> tags) {
        SetTagRequest request = new SetTagRequest();
        request.setTags(tags);
        sendRequest(Agent.MsgType.SetTag, request);
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
        sendRequest(Agent.MsgType.GetTag, request);
    }
    
    public void getId(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        getId(tags);
    }
    
    public void getId(List<Tag> tags) {
        GetIdRequest request = new GetIdRequest();
        request.setTags(tags);
        sendRequest(Agent.MsgType.GetId, request);
    }
    
    public void getIdTag(List<Tag> tags) {
    	GetIdTagRequest request = new GetIdTagRequest();
    	request.setTags(tags);
    	sendRequest(Agent.MsgType.GetIdTag, request);
    }
    
    public void getMyId() {
        sendRequest(Agent.MsgType.GetMyId, null);
    }
    
    public void setId(int id) {
        SetIdRequest request = new SetIdRequest();
        request.setId(id);
        sendRequest(Agent.MsgType.SetId, request);
    }
    
    public void listenConnection(List<Tag> tags) {
        ListenConnectionRequest request = new ListenConnectionRequest();
        request.setTags(tags);
        sendRequest(Agent.MsgType.ListenConnection, request);
    }
    
    public Serializer getSerializer() {
        return serializer;
    }
}
