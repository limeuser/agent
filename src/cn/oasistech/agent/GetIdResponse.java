package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.List;

public class GetIdResponse extends Response {
    private List<Integer> ids;
    
    public GetIdResponse() {
        super(AgentProtocol.MsgType.GetId, AgentProtocol.Error.Success);
        ids = new ArrayList<Integer>();
    }
    public List<Integer> getIds() {
        return ids;
    }
    public void setIds(List<Integer> id) {
        this.ids = id;
    }
    
    @Override 
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        str.append(", ids:");
        for (int id : ids) {
            str.append(id).append(", ");
        }
        return str.toString();
    }
}
