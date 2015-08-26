package mjoys.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mjoys.agent.util.Tag;

public class Peer<Channel> {
    private int id;
    private Channel channel;
    private Map<String, String> tags;
    private Map<String, String> listeningTags;
    
    public Peer(int id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.tags = new HashMap<String, String>();
        this.listeningTags = new HashMap<String, String>();
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

    public Map<String, String> getListenTags() {
        return listeningTags;
    }

    public void setListenTags(Map<String, String> listenTags) {
        this.listeningTags = listenTags;
    }
    
    public boolean isListening(Map<String, String> tags) {
        if (this.listeningTags.isEmpty()) {
            return false;
        }
        
        for (String key : this.listeningTags.keySet()) {
            String listeningValue = this.listeningTags.get(key);
            if (!listeningValue.isEmpty() && !listeningValue.equals(tags.get(key))) {
                return false;
            }
        }
        
        return true;
    }
}
