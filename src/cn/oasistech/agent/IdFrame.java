package cn.oasistech.agent;

// buffer = | id:4 | body-length:4 | body:body-length |
public class IdFrame {
    private int id;
    private int bodyLength;
    private byte[] body;
    
    public int getBodyLength() {
        return bodyLength;
    }
    
    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
