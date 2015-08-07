package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

import mjoys.util.Formater;

public class GetTagResponse extends Response {
    private List<IdTag> idTags;
    
    public GetTagResponse() {
        super(AgentProtocol.MsgType.GetTag, AgentProtocol.Error.Success);
        this.idTags = new ArrayList<IdTag>();
    }

    public List<IdTag> getIdTags() {
        return idTags;
    }

    public void setIdTags(List<IdTag> idTags) {
        this.idTags = idTags;
    }
    
    @Override 
    public String toString() {
        return super.toString() + ", " + Formater.formatEntry("IdTags", Formater.formatCollection(idTags));
    }
}
