package cn.oasistech.agent;

public interface AgentParser {
    public byte[] encodeRequest(Request request);
    public byte[] encodeResponse(Response response);
    public Request decodeRequest(byte[] buffer);
    public Response decodeResponse(byte[] buffer); 
}