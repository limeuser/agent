package cn.oasistech.util;

import mjoys.util.Address;

public interface Server {
    public boolean start(Address address);
    public void stop();
}
