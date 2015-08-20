package cn.oasistech.agent;

import java.nio.ByteBuffer;

public class AgentProtocol {
    public final static int IdHeadLength = 8; // | id:4 | length:4 |
    public final static int InvalidId = -1; 
    
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
        
    public static ByteBuffer writeHead(ByteBuffer buffer, int id, int length) {
        return buffer.putInt(id).putInt(length);
    }
    
    public static ByteBuffer writeHead(ByteBuffer buffer, int length) {
        return writeHead(buffer, PublicService.Agent.id, length);
    }
    
    public static ByteBuffer write(ByteBuffer buffer, int id, ByteBuffer body) {
        return writeHead(buffer, id, body.limit()).put(body);
    }
    
    public static ByteBuffer write(ByteBuffer buf, ByteBuffer body) {
        return write(buf, PublicService.Agent.id, body);
    }
    
    public static IdFrame parseIdFrame(ByteBuffer buffer) {
        int id = buffer.getInt();
        int bodyLength = buffer.getInt();
        IdFrame frame = new IdFrame();
        frame.setId(id);
        frame.setBodyLength(bodyLength);
        frame.setBody(buffer);
        return frame;
    }
}