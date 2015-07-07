package cn.oasistech.util;

import java.net.InetSocketAddress;

public class Address {
    public enum Protocol {
        Tcp,
        Udp,
        Pipe
    }
    
    private Protocol protocol;
    private String address;
    
    private Address(Protocol p, String address) {
        this.protocol = p;
        this.address = address;
    }
    
    public static Address parse(String url) {
        if (url.startsWith("tcp://")) {
            return new Address(Protocol.Tcp, url.substring("tcp://".length()));
        } else if (url.startsWith("udp://")) {
            return new Address(Protocol.Udp, url.substring("udp://".length()));
        } else if (url.startsWith("pipe://")) {
            return new Address(Protocol.Pipe, url.substring("pipe://".length()));
        }
        return null;
    }
    
    public InetSocketAddress toSocketAddress() {
        if (protocol == Protocol.Tcp || protocol == Protocol.Udp) {
            String[] ipPort = address.split(":");
            return new InetSocketAddress(ipPort[0], NumberUtil.parseInt(ipPort[1]));
        }
        return null;
    }
    
    @Override
    public String toString() {
        return protocol.name().toLowerCase() + "://" + address;
    }
}
