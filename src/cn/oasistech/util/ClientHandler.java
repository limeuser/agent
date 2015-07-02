package cn.oasistech.util;

public interface ClientHandler {
    public void handle(byte[] buffer, int offset, int length);
}
