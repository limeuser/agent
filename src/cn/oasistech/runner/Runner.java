package cn.oasistech.runner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import cn.oasistech.agent.AgentJsonParser;
import cn.oasistech.agent.AgentParser;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdKey;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentClientHandler;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.agent.server.AgentNettyServer;
import cn.oasistech.util.AsynClient;
import cn.oasistech.util.Cfg;
import cn.oasistech.util.NumberUtil;
import cn.oasistech.util.Server;
import cn.oasistech.util.SyncClient;
import cn.oasistech.util.Tag;

public class Runner {
    private static Server agentServer;
    private static InetSocketAddress server;
    private static SyncClient syncClient;
    private static AsynClient asynClient;
    private static AgentSyncRpc agentSyncRpc;
    private static AgentAsynRpc agentAsynRpc;
    
    public static void main(String[] args) {
        try {
            AgentParser parser = AgentJsonParser.instance;
            server = Cfg.getServerAddress();
            agentServer = new AgentNettyServer(AgentJsonParser.instance);
            syncClient = new SyncClient();
            asynClient = new AsynClient(new AgentClientHandler(parser));
            agentSyncRpc = new AgentSyncRpc(syncClient, parser);
            agentAsynRpc = new AgentAsynRpc(asynClient, parser);
            
            processUserCommand();
        }
        catch (Exception e) {
            System.out.println("read your input error: " + e.getMessage());
        }
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
                    agentServer.start(server.getPort());
                } else if (object.equals("client")) {
                    syncClient.start(server);
                    asynClient.start(server);
                } else if (object.equals("service")) {
                    
                }
            } else if (action.equals("stop")) {
                if (object.equals("server")) {
                    agentServer.stop();
                } else if (object.equals("client")) {
                    syncClient.stop();
                    asynClient.stop();
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
                        idKey.setId(AgentProtocol.InvalidId);
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
                agentSyncRpc.sendTo(id, msg.getBytes("UTF-8"));
                agentAsynRpc.sendTo(id, msg.getBytes("UTF-8"));
            }
            else {
                System.out.println("unknowen command");
            }
            
            echoHint();
        }
    }
}