package mjoys.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.agent.util.Tag;
import mjoys.util.Formater;

public class IdTag {
    private int id;
    private List<Tag> tags = new ArrayList<Tag>();
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public List<Tag> getTags() {
        return tags;
    }
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    @Override
    public String toString() {
        return Formater.formatEntries("id", id, "tags", Formater.formatCollection(tags));
    }
}
