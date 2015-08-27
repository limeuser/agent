package mjoys.agent.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mjoys.agent.Agent;
import mjoys.agent.GetIdRequest;
import mjoys.agent.GetIdResponse;
import mjoys.agent.GetIdTagRequest;
import mjoys.agent.GetIdTagResponse;
import mjoys.agent.GetMyIdResponse;
import mjoys.agent.GetTagRequest;
import mjoys.agent.GetTagResponse;
import mjoys.agent.IdKey;
import mjoys.agent.SetIdRequest;
import mjoys.agent.SetIdResponse;
import mjoys.agent.SetTagRequest;
import mjoys.agent.SetTagResponse;
import mjoys.agent.util.AgentCfg;
import mjoys.agent.util.Tag;
import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.Serializer;
import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.ClassUtil;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class AgentSyncRpc {
    private SocketClient client;
    private Serializer serializer;
    private byte[] recvBuffer = new byte[ByteUnit.KB];
    private byte[] sendBuffer = new byte[ByteUnit.KB];
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        this.serializer = ClassUtil.newInstance(AgentCfg.instance.getSerializerClassName());
        if (this.serializer == null) {
            return false;
        }
        
        this.client = new SocketClient();
        return client.connect(address);
    }
    
    public void stop() {
        if (this.client != null) {
            this.client.disconnect();
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
        return callAgent(Agent.MsgType.SetTag, request, SetTagResponse.class);
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
        return callAgent(Agent.MsgType.GetTag, request, GetTagResponse.class);
    }
    
    public GetIdResponse getId(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        return getId(tags);
    }
    
    public GetIdResponse getId(List<Tag> tags) {
        GetIdRequest request = new GetIdRequest();
        request.setTags(tags);
        return callAgent(Agent.MsgType.GetId, request, GetIdResponse.class);
    }
    
    public GetIdTagResponse getIdTag(List<Tag> tags) {
    	GetIdTagRequest request = new GetIdTagRequest();
    	request.setTags(tags);
    	return callAgent(Agent.MsgType.GetIdTag, request, GetIdTagResponse.class);
    }
    
    public GetMyIdResponse getMyId() {
        return callAgent(Agent.MsgType.GetMyId, null, GetMyIdResponse.class);
    }
    
    public SetIdResponse setId(int id) {
        SetIdRequest request = new SetIdRequest();
        request.setId(id);
        return callAgent(Agent.MsgType.SetId, request, SetIdResponse.class);
    }
    
    public TLV<ByteBuffer> recv() {
    	int length = 0;
    
    	try {
    		length = client.recv(recvBuffer);
    	} catch (SocketException e) {
    		client.reconnect();
    	} catch (IOException e) {
    		logger.log("agent sync rpc recv exception", e);
    	}
    	
        if (length < Agent.HeadLength) {
        	logger.log("agent sync recv bad length frame");
            return null;
        }
        
        return ByteBufferParser.parseTLV(ByteBuffer.wrap(recvBuffer, 0, length));
    }
    
    public boolean send(int id, ByteBuffer body) {
    	ByteBuffer byteBuffer = ByteBuffer.wrap(sendBuffer);
    	
    	try {
    		Agent.encodeBody(id, byteBuffer, body);
    		client.send(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
    		return true;
    	} catch (SocketException e) {
    		logger.log("agent sync rpc socket exception, reconnect", e);
    		client.reconnect();
    		return false;
    	} catch (IOException e) {
    		logger.log("agetn sync rpc send body io excetion:%s", e);
    		return false;
    	}
    }
    
    public boolean sendMsg(int id, int msgType, Object msg) {
    	try {
    		int length = Agent.encodeMsg(id, msgType, msg, ByteBuffer.wrap(sendBuffer), serializer);
    		client.send(sendBuffer, 0, length);
    		return true;
    	} catch (Exception e) {
    		logger.log("agent sync rpc send msg exception: msg=%s", e, StringUtil.toString(msg));
    		return false;
    	}
    }
    
    public <Req, Res> Res callAgent(Agent.MsgType type, Req request, Class<Res> responseClass) {
    	logger.log("sync send agent msg: %s:%s", type.name(), StringUtil.toString(request));
    	return call(Agent.PublicService.Agent.id, type.ordinal(), request, responseClass);
    }
    
    public ByteBuffer call(int id, ByteBuffer body) {
    	if (send(id, body) == false) {
    		return null;
    	}
    	
    	TLV<ByteBuffer> idFrame = recv();
		if (idFrame == null) {
			logger.log("can't get id frame");
			return null;
		}
		
		return idFrame.body;
    }
    
	public <Request, Response> Response call(int id, int type, Request request, Class<Response> responseClass) {
		if (sendMsg(id, type, request) == false) {
			return null;
		}
		
		TLV<ByteBuffer> idFrame = recv();
		TV<ByteBuffer> responseFrame = Agent.parseMsgFrame(idFrame.body);
		if (responseFrame == null) {
			logger.log("can't get response");
			return null;
		}
		
		if (responseClass == null) {
			return null;
		}
		
		try {
			return serializer.decode(new ByteBufferInputStream(responseFrame.body), responseClass);
		} catch (Exception e) {
			logger.log("decode response exception", e);
			return null;
		}
	}
}
