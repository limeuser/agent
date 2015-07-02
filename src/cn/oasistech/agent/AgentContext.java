package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.oasistech.util.IdGenerator;
import cn.oasistech.util.Tag;

public class AgentContext<Channel> {
    private IdGenerator idGenerator;
    private HashMap<Channel, Peer<Channel>> channelMap;
    private HashMap<Integer, Peer<Channel>> idMap;
    
    public AgentContext() {
        idGenerator = new IdGenerator(1);
        channelMap = new HashMap<Channel, Peer<Channel>>();
        idMap = new HashMap<Integer, Peer<Channel>>();
    }
    
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public HashMap<Channel, Peer<Channel>> getChannelMap() {
        return channelMap;
    }

    public HashMap<Integer, Peer<Channel>> getIdMap() {
        return idMap;
    }

    public List<Peer<Channel>> getPeerByTag(List<Tag> tags) {
        List<Peer<Channel>> peers = new ArrayList<Peer<Channel>>();
        for (Peer<Channel> peer : idMap.values()) {
            boolean found = true;
            for (int i = 0; i < tags.size(); i++) {
                if (!peer.getTags().get(tags.get(i).getKey()).equals(tags.get(i).getValue())) {
                    found = false;
                    break;
                }
            }
            if (found) {
                peers.add(peer);
            }
        }
        
        return peers;
    }
}
