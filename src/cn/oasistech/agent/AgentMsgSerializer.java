package cn.oasistech.agent;

public abstract class AgentMsgSerializer {
    public abstract byte[] encodeRequest(Request request);
    public abstract byte[] encodeResponse(Response response);
    
    public abstract Request decodeRequest(byte[] buffer, int offset, int length);
    public abstract Response decodeResponse(byte[] buffer, int offset, int length);
    
    public Request decodeRequest(byte[] buffer, int length) {
        return decodeRequest(buffer, 0, length);
    }
    public Request decodeRequest(byte[] buffer) {
        return decodeRequest(buffer, 0, buffer.length);
    }
    
    public Response decodeResponse(byte[] buffer, int length) {
        return decodeResponse(buffer, buffer.length);
    }
    public Response decodeResponse(byte[] buffer) {
        return decodeResponse(buffer, 0, buffer.length);
    }
}