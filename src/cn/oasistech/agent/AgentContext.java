package cn.oasistech.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mjoys.util.IdGenerator;
import cn.oasistech.util.Tag;

public class AgentContext<Channel> {
    private IdGenerator idGenerator;
    private HashMap<Integer, Peer<Channel>> idMap;
    private HashMap<Channel, Peer<Channel>> channelMap;
    
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
                String value = peer.getTags().get(tags.get(i).getKey());
                if (value == null || !value.equals(tags.get(i).getValue())) {
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
    
    public List<Peer<Channel>> getListeners(Peer<Channel> listened) {
        List<Peer<Channel>> listeningPeers = new ArrayList<Peer<Channel>>();
        for (Peer<Channel> p : getIdMap().values()) {
            if (p.isListening(listened.getTags())) {
                listeningPeers.add(p);
            }
        }
        return listeningPeers;
    }
}
