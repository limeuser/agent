package mjoys.agent;

import mjoys.util.Formater;


public class Response {
    protected Agent.Error error = Agent.Error.Success;
    
    public Agent.Error getError() {
        return error;
    }
    public void setError(Agent.Error error) {
        this.error = error;
    }
    @Override
    public String toString() {
    	return Formater.formatEntry("error", error.name());
    }
}