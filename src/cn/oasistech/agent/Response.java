package cn.oasistech.agent;

import mjoys.util.Formater;


public class Response {
    protected AgentProtocol.Error error = AgentProtocol.Error.Success;
    
    public AgentProtocol.Error getError() {
        return error;
    }
    public void setError(AgentProtocol.Error error) {
        this.error = error;
    }
    @Override
    public String toString() {
    	return Formater.formatEntry("error", error.name());
    }
}