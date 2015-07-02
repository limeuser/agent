package cn.oasistech.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.oasistech.util.Logger;

public class AgentBinaryParser implements AgentParser {
    private Logger logger = new Logger().addPrinter(System.out);
    
    @Override
    public byte[] encodeRequest(Request request) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(request);
            return out.toByteArray();
        } catch (Exception e) {
            logger.log("序列化返回信息错误", e);
            return null;
        }
    }

    @Override
    public byte[] encodeResponse(Response response) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(response);
            return out.toByteArray();
        } catch (Exception e) {
            logger.log("序列化返回信息错误", e);
            return null;
        }
    }

    @Override
    public Request decodeRequest(byte[] buffer) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream objOut = new ObjectInputStream(in);
            Object request = objOut.readObject();
            return (Request)request;
        } catch (Exception e) {
            logger.log("解析请求错误", e);
            return null;
        }
    }

    @Override
    public Response decodeResponse(byte[] buffer) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream objOut = new ObjectInputStream(in);
            Object request = objOut.readObject();
            return (Response)request;
        } catch (Exception e) {
            logger.log("解析请求错误", e);
            return null;
        }
    }

}
