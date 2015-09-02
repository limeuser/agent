package mjoys.agent;

import java.io.InputStream;
import java.nio.ByteBuffer;

import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferOutputStream;
import mjoys.io.Serializer;
import mjoys.io.SerializerException;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;

public class Agent {
	public final static int MaxFrameLength = 4 * ByteUnit.KB;
	public final static int HeadLength = 8; // | id:4 | length:4 |
    public final static int TypeFieldLength = 4;
    public final static int InvalidId = -1;
    public final static int InvalidMsgType = -1;
    
    private static Logger logger = new Logger().addPrinter(System.out);
    
    public enum MsgType {
        Unknown,
        Route,
        SetTag,
        GetTag,
        GetId,
        GetIdTag,
        GetMyId,
        SetId,
        ListenConnection,
        NotifyConnection;
    }
    
    public enum Error {
        Success,
        NoConnection,
        InternalError,
        NameParseError,
        InvalidRequest,
        BadMessageFormat,
        IdExisted,;
    }
    
    public enum PublicTag {
        id,
        servicename,
        address,
        name,
        port,
        publickey, // 公钥
        clienttype, // 同步异步
    }
    
    public enum ClientType {
        asyn,
        sync;
    }

    public enum PublicService {
        Agent(0, "agent"),
        Authentication(1, "authentication"),
        Nameing(2, "naming"),
        Ftp(3, "ftp"),
        IM(4, "im"),
        Last(2048, "last");
        
        public int id;
        public String name;
        PublicService(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    public static MsgType getMsgType(int tag) {
    	if (tag < 0 || tag >= MsgType.values().length) {
    		return MsgType.Unknown;
    	}
    	return MsgType.values()[tag];
    }
    
    public static TLV<ByteBuffer> parseIdFrame(ByteBuffer buffer) {
    	if (buffer.remaining() < HeadLength) {
    		return null;
    	}
    	
    	TLV<ByteBuffer> tlv = new TLV<ByteBuffer>();
    	tlv.tag = buffer.getInt();
    	tlv.length = buffer.getInt();
    	if (buffer.remaining() < tlv.length) {
    		return null;
    	}
    	tlv.body = buffer;
    	return tlv;
    }
    
    public static TV<ByteBuffer> parseMsgFrame(ByteBuffer buffer) {
    	if (buffer.remaining() < Agent.TypeFieldLength) {
    		return null;
    	}
    	return ByteBufferParser.parseTV(buffer);
    }
    
    public static int encodeBody(int id, ByteBuffer buffer, ByteBuffer body) {
    	buffer.putInt(id);
    	buffer.putInt(body.remaining());
    	buffer.put(body);
    	buffer.flip();
    	return buffer.remaining();
    }
    
    public static int encodeMsg(int id, int msgType, Object msg, ByteBuffer buffer, Serializer serializer) throws SerializerException {
    	int headLength = HeadLength + TypeFieldLength;
    	if (msgType == Agent.InvalidMsgType) {
    		headLength = HeadLength;
    	}
    	
    	// skip head
    	buffer.position(headLength);
    	if (msg != null) {
			serializer.encode(msg, new ByteBufferOutputStream(buffer));
    	}
    	
    	int length = buffer.position();
    	int bodyLength = buffer.position() - HeadLength;
    	buffer.position(0);
    	buffer.putInt(id);
    	buffer.putInt(bodyLength);
    	if (msgType != Agent.InvalidMsgType)
    		buffer.putInt(msgType);
    	
    	buffer.position(0);
    	buffer.limit(length);
    	return length;
    }
    
    public static Response decodeAgentResponse(MsgType type, InputStream in, Serializer serializer) {
    	Response response = null;
    	try {
    		switch (type) {
    		case SetId:
    			response = serializer.decode(in, SetIdResponse.class);
    			break;
    		case GetId:
    			response = serializer.decode(in, GetIdResponse.class);
    			break;
    		case GetIdTag:
    			response = serializer.decode(in, GetIdTagResponse.class);
    			break;
    		case GetMyId:
    			response = serializer.decode(in, GetMyIdResponse.class);
    			break;
    		case GetTag:
    			response = serializer.decode(in, GetTagResponse.class);
    			break;
    		case ListenConnection:
    			response = serializer.decode(in, ListenConnectionResponse.class);
    			break;
    		case NotifyConnection:
    			response = serializer.decode(in, NotifyConnectionResponse.class);
    			break;
    		case SetTag:
    			response = serializer.decode(in, SetTagResponse.class);
    			break;
    		case Unknown:
    			response = serializer.decode(in, Response.class);
    			break;
    		case Route:
    			break;
    		default:
    			break;
    		}
    	} catch (Exception e) {
    		logger.log("decode response exception", e);
    		return null;
    	}
    	return response;
    }
}