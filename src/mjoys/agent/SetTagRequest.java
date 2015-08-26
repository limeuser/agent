package mjoys.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.agent.util.Tag;
import mjoys.util.Formater;

public class SetTagRequest {
    private List<Tag> tags;
    
    public SetTagRequest() {
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
        return Formater.formatEntry("Tags", Formater.formatCollection(tags));
    }
}
