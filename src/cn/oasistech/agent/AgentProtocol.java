package cn.oasistech.agent;

import java.io.ByteArrayOutputStream;

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
        
    public static int writeHead(ByteArrayOutputStream buf, int id, int length) {
        return writeInt32(buf, id) + writeInt32(buf, length);
    }
    
    public static int writeHead(ByteArrayOutputStream buf, int length) {
        return writeHead(buf, PublicService.Agent.id, length);
    }
    
    public static int write(ByteArrayOutputStream buf, int id, byte[] body, int offset, int length) {
        writeHead(buf, id, length);
        buf.write(body, offset, length);
        return IdHeadLength + length;
    }
    
    public static int write(ByteArrayOutputStream buf, int id, byte[] body) {
        return write(buf, id, body, 0, body.length);
    }
    
    public static int write(ByteArrayOutputStream buf, byte[] body, int offset, int length) {
        return write(buf, PublicService.Agent.id, body, offset, length);
    }
    
    public static int write(ByteArrayOutputStream buf, byte[] body) {
        return write(buf, body, 0, body.length);
    }
    
    public static IdFrame parseIdFrame(byte[] buffer) {
        int id = readInt32(buffer, 0);
        int bodyLength = readInt32(buffer, 4);
        byte[] body = new byte[bodyLength];
        System.arraycopy(buffer, IdHeadLength, body, 0, bodyLength);
        IdFrame frame = new IdFrame();
        frame.setId(id);
        frame.setBodyLength(bodyLength);
        frame.setBody(body);
        return frame;
    }
    
    public static int writeInt32(ByteArrayOutputStream buf, int value) {
        buf.write((byte)(value >>> 24));
        buf.write((byte)(value >>> 16));
        buf.write((byte)(value >>> 8));
        buf.write((byte)value);
        return 4;
    }
    
    public static int readInt32(byte[] buffer, int offset) {
        int value = 0;
        for (int i = 0; i < 3; i++) {
            if (buffer[i + offset] >= 0) {
                value = value + buffer[i + offset];
            } else {
                value = value + 256 + buffer[i + offset];
            }
            value = value * 256;
        }
        if (buffer[3 + offset] >= 0) {
            value = value + buffer[3 + offset];
        } else {
            value = value + 256 + buffer[3 + offset];
        }
        return value;
    }
}