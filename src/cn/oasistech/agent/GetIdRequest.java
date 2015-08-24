package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;
import cn.oasistech.util.Tag;

public class GetIdRequest {
    private List<Tag> tags;
    
    public GetIdRequest() {
        this.tags = new ArrayList<Tag>();
    }
    public List<Tag> getTags() {
        return tags;
    }
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    @Override 
    public String toString() {
        return Formater.formatEntry("tags", Formater.formatCollection(tags));
    }
}
