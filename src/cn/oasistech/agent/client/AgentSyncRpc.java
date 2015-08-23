package cn.oasistech.agent.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
import cn.oasistech.agent.SetIdRequest;
import cn.oasistech.agent.SetIdResponse;
import cn.oasistech.agent.SetTagRequest;
import cn.oasistech.agent.SetTagResponse;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.Tag;

public class AgentSyncRpc {
    private SocketClient client;
    private Serializer serializer;
    private byte[] recvBuffer = new byte[ByteUnit.KB];
    private byte[] sendBuffer = new byte[ByteUnit.KB];
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        this.serializer = ClassUtil.newInstance(Cfg.getParserClassName());
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
        return callAgent(AgentProtocol.MsgType.SetTag, request, SetTagResponse.class);
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
        return callAgent(AgentProtocol.MsgType.GetTag, request, GetTagResponse.class);
    }
    
    public GetIdResponse getId(Tag tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);
        return getId(tags);
    }
    
    public GetIdResponse getId(List<Tag> tags) {
        GetIdRequest request = new GetIdRequest();
        request.setTags(tags);
        return callAgent(AgentProtocol.MsgType.GetId, request, GetIdResponse.class);
    }
    
    public GetIdTagResponse getIdTag(List<Tag> tags) {
    	GetIdTagRequest request = new GetIdTagRequest();
    	request.setTags(tags);
    	return callAgent(AgentProtocol.MsgType.GetIdTag, request, GetIdTagResponse.class);
    }
    
    public GetMyIdResponse getMyId() {
        GetMyIdRequest request = new GetMyIdRequest();
        return callAgent(AgentProtocol.MsgType.GetMyId, request, GetMyIdResponse.class);
    }
    
    public SetIdResponse setId(int id) {
        SetIdRequest request = new SetIdRequest();
        request.setId(id);
        return callAgent(AgentProtocol.MsgType.SetId, request, SetIdResponse.class);
    }
    
    public TV<ByteBuffer> recv() {
    	int length = 0;
    
    	try {
    		length = client.recv(recvBuffer);
    	} catch (SocketException e) {
    		client.reconnect();
    	} catch (IOException e) {
    		logger.log("agent sync rpc recv exception", e);
    	}
    	
        if (length < AgentProtocol.HeadLength) {
            return null;
        }
        
        TLV<ByteBuffer> idFrame = ByteBufferParser.parseTLV(ByteBuffer.wrap(recvBuffer, 0, length));
        return AgentProtocol.parseMsgFrame(idFrame.body);
    }
    
    public boolean send(int id, ByteBuffer body) {
    	ByteBuffer byteBuffer = ByteBuffer.wrap(sendBuffer);
    	
    	try {
    		AgentProtocol.encodeBody(id, byteBuffer, body);
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
    		int length = AgentProtocol.encodeMsg(id, msgType, msg, ByteBuffer.wrap(sendBuffer), serializer);
    		client.send(sendBuffer, 0, length);
    		logger.log("agent sync rpc send msg: msg=%s", msg.toString());
    		return true;
    	} catch (Exception e) {
    		logger.log("agent sync rpc send msg exception: msg=%s", e, msg.toString());
    		return false;
    	}
    }
    
    public <Req, Res> Res callAgent(AgentProtocol.MsgType type, Req request, Class<Res> responseClass) {
    	return call(AgentProtocol.PublicService.Agent.id, type.ordinal(), request, responseClass);
    }
    
    public ByteBuffer call(int id, ByteBuffer body) {
    	if (send(id, body) == false) {
    		return null;
    	}
    	
    	TV<ByteBuffer> responseFrame = recv();
		if (responseFrame == null) {
			logger.log("can't get response");
			return null;
		}
		
		return responseFrame.body;
    }
    
	public <Request, Response> Response call(int id, int type, Request request, Class<Response> responseClass) {
		if (sendMsg(id, type, request) == false) {
			return null;
		}
		
		TV<ByteBuffer> responseFrame = recv();
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
