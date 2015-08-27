package mjoys.agent.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mjoys.agent.Agent;
import mjoys.agent.IdKey;
import mjoys.agent.Response;
import mjoys.agent.client.AgentAsynRpc;
import mjoys.agent.client.AgentRpcHandler;
import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.server.AgentNettyServer;
import mjoys.agent.util.AgentCfg;
import mjoys.agent.util.Tag;
import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.util.Address;
import mjoys.util.NumberUtil;
import mjoys.util.StringUtil;

public class Main {
    private static AgentNettyServer agentServer;
    private static Address agentAddress;
    private static AgentSyncRpc agentSyncRpc;
    private static AgentAsynRpc agentAsynRpc;
    
    public static void main(String[] args) throws IOException {
        agentAddress = Address.parse(AgentCfg.instance.getServerAddress());
        agentServer = new AgentNettyServer();
        agentSyncRpc = new AgentSyncRpc();
        agentAsynRpc = new AgentAsynRpc();

        processUserCommand();
    }
    
    private static void echoHint() {
        System.out.print("agent: ");
    }
    private static void echo(String msg) {
        echoHint();
        System.out.println(msg);
    }
    
    private static void processUserCommand() throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in)); 
        String line;
        
        echoHint();
        while ((line = reader.readLine()) != null) {
            echoHint();
            
            String[] tokens = line.split("\\s");
            if (tokens.length < 2) {
                echo("bad command");
                continue;
            }
            
            String action = tokens[0];
            String object = tokens[1];
            if (action.equals("start")) {
                if (object.equals("server")) {
                    agentServer.start(agentAddress);
                }
                else if (object.equals("client")) {
                    agentSyncRpc.start(agentAddress);
                    agentAsynRpc.start(agentAddress, new LogHandler());
                }
            } else if (action.equals("stop")) {
                if (object.equals("server")) {
                    agentServer.stop();
                } else if (object.equals("client")) {
                    agentSyncRpc.stop();
                    agentAsynRpc.stop();
                }
            } 
            // get tag id1 id2 id3 key1 key2 ...
            // get id key1 value1 key2 value2 ...
            else if (action.equals("get")) {
                if (object.equals("tag")) {
                    List<Integer> ids = new ArrayList<Integer>();
                    int i = 2;
                    for (; i < tokens.length; i++) {
                        Integer id = NumberUtil.parseInt(tokens[i]);
                        if (id == null) {
                            i--;
                            break;
                        }
                        ids.add(id);
                    }
                    
                    List<String> keys = new ArrayList<String>();
                    for (; i < tokens.length; i++) {
                        keys.add(tokens[i]);
                    }
                    
                    List<IdKey> idKeys = new ArrayList<IdKey>();
                    if (ids.isEmpty()) {
                        IdKey idKey = new IdKey();
                        idKey.setId(Agent.InvalidId);
                        idKey.setKeys(keys);
                        idKeys.add(idKey);
                    } else {
                        for (int id : ids) {
                            IdKey idKey = new IdKey();
                            idKey.setId(id);
                            idKey.setKeys(keys);
                            idKeys.add(idKey);
                        }
                    }
                    agentSyncRpc.getTag(idKeys);
                    agentAsynRpc.getTag(idKeys);
                } else if (object.equals("id")) {
                    if (tokens.length < 2 || tokens.length %2 != 0) {
                        echo("bad command");
                        continue;
                    }
                    List<Tag> tags = new ArrayList<Tag>();
                    for (int i = 2; i < tokens.length; i+=2) {
                        tags.add(new Tag(tokens[i], tokens[i+1]));
                    }
                    agentSyncRpc.getId(tags);
                    agentAsynRpc.getId(tags);
                } else if (object.equals("myid")) {
                    agentSyncRpc.getMyId();
                    agentAsynRpc.getMyId();
                }
            } 
            // set tag key1 value1 key2 value2 ...
            // set id newid
            else if (action.equals("set")) {
                if (object.equals("tag")) {
                    List<Tag> tags = new ArrayList<Tag>();
                    for (int i = 2; i < tokens.length; i+=2) {
                        tags.add(new Tag(tokens[i], tokens[i+1]));
                    }
                    agentSyncRpc.setTag(tags);
                    agentAsynRpc.setTag(tags);
                } else if (object.equals("id")) {
                    int newId = NumberUtil.parseInt(tokens[2]);
                    agentSyncRpc.setId(newId);
                    agentAsynRpc.setId(newId);
                }
            }
            else if (action.equals("sendto")) {
                Integer id = NumberUtil.parseInt(tokens[1]);
                if (id == null) {
                    echo("bad command");
                    continue;
                }
                String msg = tokens[2];
                agentSyncRpc.send(id, ByteBuffer.wrap(StringUtil.toBytes(msg, "UTF-8")));
                agentAsynRpc.send(id, ByteBuffer.wrap(StringUtil.toBytes(msg, "UTF-8")));
            }
            else {
                System.out.println("unknowen command");
            }
            
            echoHint();
        }
    }
    
    public static class LogHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> idFrame) {
        	TV<ByteBuffer> msgFrame = ByteBufferParser.parseTV(idFrame.body);
        	Agent.MsgType msgType = Agent.getMsgType(msgFrame.tag);
        	if (msgType == Agent.MsgType.Unknown) {
        		System.out.println("recv unknown message");
        		return;
        	}
        	Response response = Agent.decodeAgentResponse(msgType, new ByteBufferInputStream(msgFrame.body), rpc.getSerializer());
            System.out.println("recv message from " + idFrame.tag + ": " + msgType.name() + ":" + response.toString());
        }
    }
}