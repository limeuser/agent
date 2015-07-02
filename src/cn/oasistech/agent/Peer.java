package cn.oasistech.agent;

import java.util.HashMap;
import java.util.Map;

public class Peer<Channel> {
    private int id;
    private Channel channel;
    private Map<String, String> tags;
    
    public Peer(int id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.setTags(new HashMap<String, String>());
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id=").append(id);
        
        if (channel == null) {
            str.append(", channel=").append("null");
        } else {
            str.append(", addr=").append(channel.toString());
        }
        
        return str.toString();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
