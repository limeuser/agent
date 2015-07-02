package cn.oasistech.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private AtomicInteger id;
    
    public IdGenerator(int first) {
        id = new AtomicInteger(first);
    }
    
    public int getId() {
        return id.getAndIncrement();
    }
}