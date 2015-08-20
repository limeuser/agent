package cn.oasistech.agent;

// buffer = | id:4 | body-length:4 | body:body-length |
public class IdFrame<Buffer> {
    private int id;
    private int bodyLength;
    private Buffer body;
    
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

    public Buffer getBody() {
        return body;
    }

    public void setBody(Buffer body) {
        this.body = body;
    }
}
