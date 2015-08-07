package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;
import cn.oasistech.util.Tag;

public class SetTagRequest extends Request {
    private List<Tag> tags;
    
    public SetTagRequest() {
        super(AgentProtocol.MsgType.SetTag);
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
        return super.toString() + ", " + Formater.formatEntry("Tags", Formater.formatCollection(tags));
    }
}
