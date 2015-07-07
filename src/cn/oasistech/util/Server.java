package cn.oasistech.util;

public interface Server {
    public boolean start(Address address);
    public void stop();
}
