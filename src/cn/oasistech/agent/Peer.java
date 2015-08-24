package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.oasistech.util.Tag;

public class Peer<Channel> {
    private int id;
    private Channel channel;
    private Map<String, String> tags;
    private List<IdTag> listeners;
    
    public Peer(int id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.tags = new HashMap<String, String>();
        this.listeners = new ArrayList<IdTag>();
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
    
    public List<Tag> getTagList() {
        List<Tag> tagList = new ArrayList<Tag>();
        for (String key : this.tags.keySet()) {
            tagList.add(new Tag(key, this.tags.get(key)));
        }
        return tagList;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<IdTag> getListeners() {
        return listeners;
    }

    public void setListeners(List<IdTag> listeners) {
        this.listeners = listeners;
    }
}
