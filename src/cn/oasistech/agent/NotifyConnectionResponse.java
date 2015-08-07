package cn.oasistech.agent;

import mjoys.util.Formater;

public class NotifyConnectionResponse extends Response {
    private IdTag idTag;
    private Action action;
    
    public NotifyConnectionResponse() {
        super(AgentProtocol.MsgType.NotifyConnection, AgentProtocol.Error.Success);
    }

    public IdTag getIdTag() {
        return idTag;
    }

    public void setIdTag(IdTag idTag) {
        this.idTag = idTag;
    }
    
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + Formater.formatEntries("IdTag", idTag.toString(), "Action", action.name());
    }

    public enum Action {
        connect,
        disconnect,
    }
}
