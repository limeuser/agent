package cn.oasistech.agent;

import cn.oasistech.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AgentJsonParser implements AgentParser {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    private AgentJsonParser() {}
    public final static AgentParser instance = new AgentJsonParser();
    
    @Override
    public byte[] encodeRequest(Request request) {
        try {
            return mapper.writeValueAsBytes(request);
        } catch (Exception e) {
            logger.log("序列化请求消息错误", e);
            return null;
        }
    }

    @Override
    public byte[] encodeResponse(Response response) {
        try {
            return mapper.writeValueAsBytes(response);
        } catch (Exception e) {
            logger.log("序列化回应消息错误", e);
            return null;
        }
    }

    @Override
    public Request decodeRequest(byte[] buffer) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(buffer);
            String type = root.path("type").asText();
            
            if (type.equalsIgnoreCase(AgentProtocol.MsgType.SetTag.name())) {
                return mapper.readValue(buffer, SetTagRequest.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.GetTag.name())) {
                return mapper.readValue(buffer, GetTagRequest.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.GetId.name())) {
                return mapper.readValue(buffer, GetIdRequest.class);
            } 
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.SetId.name())) {
                return mapper.readValue(buffer, SetIdRequest.class);
            }
            else {
                return null;
            }
        } catch(Exception e) {
            logger.log("解析请求错误", e);
            return null;
        }
    }

    @Override
    public Response decodeResponse(byte[] buffer) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(buffer);
            String type = root.path("type").asText();
            if (type.equalsIgnoreCase(AgentProtocol.MsgType.SetTag.name())) {
                return mapper.readValue(buffer, SetTagResponse.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.GetTag.name())) {
                return mapper.readValue(buffer, GetTagResponse.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.GetId.name())) {
                return mapper.readValue(buffer, GetIdResponse.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.SetId.name())) {
                return mapper.readValue(buffer, SetIdResponse.class);
            }
            else if (type.equalsIgnoreCase(AgentProtocol.MsgType.Unknown.name())) {
                return mapper.readValue(buffer, Response.class);
            } 
            else {
                return null;
            }
        } catch(Exception e) {
            logger.log("解析请求错误", e);
            return null;
        }
    }
}
