package cn.oasistech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.util.Address;
import cn.oasistech.util.IdGenerator;
import cn.oasistech.util.Logger;
import cn.oasistech.util.Tag;

public class ClusterManager {
    private List<Host> members = new ArrayList<Host>();
    private final static IdGenerator hostIdGenerator = new IdGenerator(1);
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public void start(String address) {
        Host host = Host.connect(Address.parse(address));
        if (host != null) {
            members.add(host);
        } else {
            logger.log("cant't connect address");
        }
    }
    
    public void sentTo(String service, byte[] body, Host ...hosts) {
        for (Host host : hosts) {
            GetIdResponse response = host.getRpc().getId(new Tag(AgentProtocol.PublicTag.servicename.name(), service));
            for (int id : response.getIds()) {
               host.getRpc().sendTo(id, body); 
            }
        }
    }
    
    public void sendToAll(String service, byte[] body) {
        Host[] hosts = new Host[members.size()];
        members.toArray(hosts);
        sentTo(service, body, hosts);
    }
    
    public static class Host {
        private int id;
        private String name;
        private AgentSyncRpc rpc;
        private Map<String, Integer> services;
        
        public static Host connect(Address address) {
            Host host = new Host();
            host.rpc = new AgentSyncRpc();
            if (host.rpc.start(address) == false) {
                return null;
            }
            
            host.id = hostIdGenerator.getId();
            host.name = address.toString();
            host.services = new HashMap<String, Integer>();
            return host;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AgentSyncRpc getRpc() {
            return rpc;
        }

        public void setRpc(AgentSyncRpc rpc) {
            this.rpc = rpc;
        }

        public Map<String, Integer> getServices() {
            return services;
        }

        public void setServices(Map<String, Integer> services) {
            this.services = services;
        }
    }
}
